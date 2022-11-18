package com.gevamu.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.contracts.PaymentContract
import com.gevamu.states.Payment
import java.time.Instant
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByService
@StartableByRPC
class StatusUpdateFlow(
    private val paymentId: UniqueIdentifier,
    private val paymentStatus: Payment.PaymentStatus,
    private val additionalInfo: String,
    private val timestamp: Instant,
) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // TODO This flow may be executed concurrently with the same paymentId.
        //      To address it, flow should update state only if timestamp is newer than timestamp from the last state
        //      and retry the flow if "Input state consumed" exception is raised.
        //      Note. Besides timestamp comparison, additional criteria may be needed as timestamp may come from
        //      different sources which aren't synchronized.

        // Find sent payment state to use as an input
        val inputCriteria: QueryCriteria.LinearStateQueryCriteria = QueryCriteria.LinearStateQueryCriteria()
            .withUuid(listOf(paymentId.id))
            // State should be unconsumed as it will be used as input state for transaction
            .withStatus(Vault.StateStatus.UNCONSUMED)
            // Gateway node is a participant
            .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT)
        val foundPaymentStates = serviceHub.vaultService.queryBy(Payment::class.java, inputCriteria).states
        if (foundPaymentStates.isEmpty()) {
            logger.error("No payments found for id={}", paymentId.id)
            // XXX Send HTTP reply with error status if flow started on push notification?
            return
        }
        if (foundPaymentStates.size > 1) {
            throw RuntimeException(
                "Internal error: query for payment state with id $paymentId returned ${foundPaymentStates.size} results"
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
