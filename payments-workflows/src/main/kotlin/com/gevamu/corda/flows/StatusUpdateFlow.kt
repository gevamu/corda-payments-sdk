/*
 * Copyright 2022 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gevamu.corda.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.corda.contracts.PaymentContract
import com.gevamu.corda.schema.PaymentSchemaV1
import com.gevamu.corda.states.Payment
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant
import java.util.UUID
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowException
import net.corda.core.flows.NotaryError
import net.corda.core.flows.NotaryException
import net.corda.core.flows.NotaryFlow
import net.corda.core.node.ServiceHub
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction

data class PaymentStatusUpdate(
    val paymentStatus: Payment.PaymentStatus,
    val additionalInfo: String,
    val timestamp: Instant,
)

// Multiple status updates for the same payment request may be executed concurrently.
@InitiatingFlow
@StartableByService
class StatusUpdateFlow(
    private val inputStateAndRef: StateAndRef<Payment>,
    private val statusUpdate: PaymentStatusUpdate,
) : FlowLogic<Unit>() {
    @CordaSerializable
    enum class FlowStep {
        SIGN_TRANSACTION,
        FINALIZE
    }

    @Suspendable
    override fun call() {
        val uniquePaymentId = inputStateAndRef.state.data.uniquePaymentId
        logger.debug(
            "Updating report for payment request id={} with status={}",
            uniquePaymentId, statusUpdate.paymentStatus
        )
        // TODO If timestamp of input state is newer than timestamp of the payment update, don't record this update.
        //      Note. Besides timestamp comparison, additional criteria may be needed as timestamps may come from
        //      different sources which aren't synchronized.

        // Initiate update status flow with original participants
        val paymentSession = initiateFlow(inputStateAndRef.state.data.payer)
        // TODO As of 2022/10/26 it has only the payer party and should use [participants] instead going forward
        // val sessions = paymentUpdate.participants.map { initiateFlow(it) }

        var inputState = this.inputStateAndRef
        var notarizedTx: SignedTransaction? = null
        do {
            // Update payment with the params from the bank's response
            val paymentUpdate = inputState.state.data.copy(
                status = statusUpdate.paymentStatus,
                additionalInfo = statusUpdate.additionalInfo,
                timestamp = statusUpdate.timestamp,
            )

            // Build and prepare state and contract on gateway
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val builder = TransactionBuilder(notary)
                // FIXME change to reference flow?
                .addInputState(inputState)
                .addOutputState(paymentUpdate)
                .addCommand(
                    PaymentContract.Commands.UpdateStatus(uniquePaymentId),
                    ourIdentity.owningKey,
                    paymentUpdate.payer.owningKey
                )
                .addAttachment(paymentUpdate.paymentInstructionId)
            builder.verify(serviceHub)

            // Sign the transaction on gateway
            val partSignedTx = serviceHub.signInitialTransaction(builder)

            // Ask participating payer to sign
            val signedTx = subFlow(CollectSignaturesFlow(partSignedTx, listOf(paymentSession)))

            try {
                notarizedTx = signedTx + subFlow(NotaryFlow.Client(signedTx, skipVerification = true))
            } catch (error: NotaryException) {
                val notaryError = error.error
                if (notaryError is NotaryError.Conflict && notaryError.consumedStates.containsKey(inputState.ref)) {
                    logger.debug("Report state for payment request id={} is consumed, retrying", uniquePaymentId)
                    inputState = queryLastPaymentState(serviceHub, uniquePaymentId)
                    // TODO If timestamp of returned state is newer than timestamp of the payment report update,
                    //      don't record this update.
                    //      Note. Besides timestamp comparison, additional criteria may be needed as timestamps may come
                    //      from different sources which aren't synchronized.
                    paymentSession.send(FlowStep.SIGN_TRANSACTION, maySkipCheckpoint = true)
                } else {
                    throw error
                }
            }
        } while (notarizedTx == null)

        paymentSession.send(FlowStep.FINALIZE, maySkipCheckpoint = true)
        subFlow(FinalityFlow(notarizedTx, listOf(paymentSession)))
    }

    companion object {
        fun queryLastPaymentState(serviceHub: ServiceHub, uniquePaymentId: UUID): StateAndRef<Payment> {
            // Find sent payment state by id to use as an input
            val queryCriteria = builder {
                QueryCriteria.VaultCustomQueryCriteria(
                    expression = PaymentSchemaV1.PersistentPayment::uniquePaymentId.equal(uniquePaymentId),
                    // State should be unconsumed as it will be used as input state for transaction
                    status = Vault.StateStatus.UNCONSUMED,
                    // Gateway node is a participant
                    relevancyStatus = Vault.RelevancyStatus.RELEVANT,
                )
            }
            val foundPaymentStates = serviceHub.vaultService.queryBy<Payment>(queryCriteria).states
            // XXX Consider throwing other exception type to send flow to the hospital instead of terminating it.
            //     In theory this condition can be fixed leading to successful flow execution.
            if (foundPaymentStates.isEmpty()) {
                throw FlowException("No unconsumed payment state is found for id=$uniquePaymentId")
            }
            if (foundPaymentStates.size > 1) {
                throw FlowException(
                    "Internal error: query for payment state with id=$uniquePaymentId returned ${foundPaymentStates.size} states"
                )
            }
            return foundPaymentStates.first()
        }
    }
}
