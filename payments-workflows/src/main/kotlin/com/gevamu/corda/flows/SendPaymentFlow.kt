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
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import java.time.Instant
import java.util.UUID

@InitiatingFlow
@StartableByService
class SendPaymentFlow(private val uniquePaymentId: UUID) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        logger.debug("Send payment id={}", uniquePaymentId)
        val paymentStateAndRef = builder {
            // Find all active Payments for the given ID using its JPA entity
            val criteria = QueryCriteria.VaultCustomQueryCriteria(
                expression = PaymentSchemaV1.PersistentPayment::uniquePaymentId.equal(uniquePaymentId),
                // Do not use archived consumed states
                // Because we will use the newest Payment state as input
                status = Vault.StateStatus.UNCONSUMED,
                // Payer node is a participant
                relevancyStatus = Vault.RelevancyStatus.RELEVANT,
            )
            // TODO Check that only one is returned
            //      and status is CREATED or add status to the query criteria
            serviceHub.vaultService.queryBy<Payment>(criteria).states.first()
        }
        logger.debug("Found payment stateAndRef={}", paymentStateAndRef)

        val gateway = paymentStateAndRef.state.data.gateway

        val gatewaySession: FlowSession = initiateFlow(gateway)

        val defaultPaymentProvider: UUID = gatewaySession.receive(UUID::class.java).unwrap { it }

        // Mutate it to change status and timestamp
        val payment = paymentStateAndRef.state.data.run {
            copy(
                status = Payment.PaymentStatus.SENT_TO_GATEWAY,
                timestamp = Instant.now(),
                paymentProviderId = if (paymentProviderId == null) defaultPaymentProvider else paymentProviderId
            )
        }

        // Prepare transaction to send over the state
        // TODO It's better to replace notary with named one since we are consuming states
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary)
            .addInputState(paymentStateAndRef)
            .addOutputState(payment)
            .addCommand(PaymentContract.Commands.SendToGateway(uniquePaymentId), ourIdentity.owningKey, gateway.owningKey)
            .addAttachment(paymentStateAndRef.state.data.paymentInstructionId)
        builder.verify(serviceHub)

        // Sign the transaction on our side
        val partiallySignedTransaction = serviceHub.signInitialTransaction(builder)

        // Expect gateway to sign the transaction
        val fullySignedTransaction = subFlow(CollectSignaturesFlow(partiallySignedTransaction, listOf(gatewaySession)))

        // Record signed transaction to the vault
        subFlow(FinalityFlow(fullySignedTransaction, listOf(gatewaySession)))
    }
}
