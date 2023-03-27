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
import net.corda.core.node.services.vault.Builder.`in`
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant
import java.util.UUID

@InitiatingFlow
@StartableByService
class StatusUpdateFlow(
    private val uniquePaymentIds: Collection<UUID>,
    private val paymentStatus: Payment.PaymentStatus,
    private val additionalInfo: String,
    private val timestamp: Instant,
) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        logger.info("Update payment id=$uniquePaymentIds with status=$paymentStatus")
        // TODO This flow may be executed concurrently with the same paymentId.
        //      To address it, flow should update state only if timestamp is newer than timestamp from the last state
        //      and retry the flow if "Input state consumed" exception is raised.
        //      Note. Besides timestamp comparison, additional criteria may be needed as timestamp may come from
        //      different sources which aren't synchronized.

        // Find sent payment state by id to use as an input
        val idEqualsCriteria = QueryCriteria.VaultCustomQueryCriteria(
            expression = PaymentSchemaV1.PersistentPayment::uniquePaymentId.`in`(uniquePaymentIds),
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
            logger.error("No payments found for id={}", uniquePaymentIds)
            // XXX Send HTTP reply with error status if flow started on push notification?
            return
        }

        // Update payment with the params from the bank's response
        val paymentUpdate = foundPaymentStates.map {
            it.state.data.copy(
                status = paymentStatus,
                additionalInfo = additionalInfo,
                timestamp = timestamp,
            )
        }

        // Build and prepare state and contract on gateway
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val payerParty = paymentUpdate.first().payer
        val builder = TransactionBuilder(notary)
            .addCommand(PaymentContract.Commands.UpdateStatus(uniquePaymentId), ourIdentity.owningKey, payerParty.owningKey)
            .addAttachment(paymentUpdate.first().paymentInstructionId)
        foundPaymentStates.forEach { builder.addInputState(it) }
        paymentUpdate.forEach { builder.addOutputState(it) }
        builder.verify(serviceHub)

        // Sign the transaction on gateway
        val partSignedTx = serviceHub.signInitialTransaction(builder)

        // Initiate update status flow with original participants
        val paymentSession = initiateFlow(payerParty)
        // TODO As of 2022/10/26 it has only the payer party and should use [participants] instead going forward
        // val sessions = paymentUpdate.participants.map { initiateFlow(it) }

        // Ask participating payer to sign
        val signedTx = subFlow(CollectSignaturesFlow(partSignedTx, listOf(paymentSession)))

        // Finish and persist state and tx
        subFlow(FinalityFlow(signedTx, listOf(paymentSession)))
    }
}
