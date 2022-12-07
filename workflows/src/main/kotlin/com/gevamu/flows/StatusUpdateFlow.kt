package com.gevamu.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.contracts.PaymentContract
import com.gevamu.schema.PaymentSchemaV1
import com.gevamu.states.Payment
import java.time.Instant
import java.util.UUID
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

@InitiatingFlow
@StartableByService
class StatusUpdateFlow(
    private val uniquePaymentId: UUID,
    private val paymentStatus: Payment.PaymentStatus,
    private val additionalInfo: String,
    private val timestamp: Instant,
) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        logger.info("Update payment id=$uniquePaymentId with status=$paymentStatus")
        // TODO This flow may be executed concurrently with the same paymentId.
        //      To address it, flow should update state only if timestamp is newer than timestamp from the last state
        //      and retry the flow if "Input state consumed" exception is raised.
        //      Note. Besides timestamp comparison, additional criteria may be needed as timestamp may come from
        //      different sources which aren't synchronized.

        // Find sent payment state by id to use as an input
        val idEqualsCriteria = QueryCriteria.VaultCustomQueryCriteria(
            expression = PaymentSchemaV1.PersistentPayment::uniquePaymentId.equal(uniquePaymentId),
            // State should be unconsumed as it will be used as input state for transaction
            status = Vault.StateStatus.UNCONSUMED,
            // Gateway node is a participant
            relevancyStatus = Vault.RelevancyStatus.RELEVANT,
        )

        val foundPaymentStates = builder {
            // all states from gateway node
            serviceHub.vaultService.queryBy<Payment>(idEqualsCriteria)
                // Get state and ref list because we need to consume found state in the flow
                .states
        }
        if (foundPaymentStates.isEmpty()) {
            logger.error("No payments found for id={}", uniquePaymentId)
            // XXX Send HTTP reply with error status if flow started on push notification?
            return
        }
        if (foundPaymentStates.size > 1) {
            throw RuntimeException(
                "Internal error: query for payment state with id=$uniquePaymentId returned ${foundPaymentStates.size} states"
            )
        }
        val paymentStateAndRef = foundPaymentStates.first()

        // Update payment with the params from the bank's response
        val paymentUpdate = paymentStateAndRef.state.data.copy(
            status = paymentStatus,
            additionalInfo = additionalInfo,
            timestamp = timestamp,
        )

        // Build and prepare state and contract on gateway
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary)
            // FIXME change to reference flow?
            .addInputState(paymentStateAndRef)
            .addOutputState(paymentUpdate)
            .addCommand(PaymentContract.Commands.UpdateStatus(), ourIdentity.owningKey, paymentUpdate.payer.owningKey)
            .addAttachment(paymentUpdate.paymentInstructionId)
        builder.verify(serviceHub)

        // Sign the transaction on gateway
        val partSignedTx = serviceHub.signInitialTransaction(builder)

        // Initiate update status flow with original participants
        val paymentSession = initiateFlow(paymentUpdate.payer)
        // TODO As of 2022/10/26 it has only the payer party and should use [participants] instead going forward
        //val sessions = paymentUpdate.participants.map { initiateFlow(it) }

        // Ask participating payer to sign
        val signedTx = subFlow(CollectSignaturesFlow(partSignedTx, listOf(paymentSession)))

        // Finish and persist state and tx
        subFlow(FinalityFlow(signedTx, listOf(paymentSession)))
    }
}
