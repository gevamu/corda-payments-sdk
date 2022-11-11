package com.gevamu.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.contracts.PaymentContract
import com.gevamu.states.Payment
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.time.Instant

@InitiatingFlow
@StartableByService
class SendPaymentFlow(private val paymentId: UniqueIdentifier) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        // Get payment from the vault
        val inputCriteria: QueryCriteria.LinearStateQueryCriteria = QueryCriteria.LinearStateQueryCriteria()
            .withUuid(listOf(paymentId.id))
            // Shouldn't be archived
            .withStatus(Vault.StateStatus.UNCONSUMED)
            // Payer should be a participant
            .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT)
        val paymentStateAndRef = serviceHub.vaultService.queryBy(Payment::class.java, inputCriteria).states.first()

        // Mutate it to change status and timestamp
        val payment = paymentStateAndRef.state.data.copy(
            status = Payment.PaymentStatus.SENT_TO_GATEWAY,
            timestamp = Instant.now(),
        )

        // Prepare transaction to send over the state
        // TODO It's better to replace notary with named one since we are consuming states
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val gateway = paymentStateAndRef.state.data.gateway
        val builder = TransactionBuilder(notary)
            .addInputState(paymentStateAndRef)
            .addOutputState(payment)
            .addCommand(PaymentContract.Commands.SendToGateway(), ourIdentity.owningKey, gateway.owningKey)
            .addAttachment(paymentStateAndRef.state.data.paymentInstructionId)
        builder.verify(serviceHub)

        // Sign the transaction on our payer cordapp
        val partiallySignedTransaction = serviceHub.signInitialTransaction(builder)

        // Notify gateway about incoming payment tx
        val gatewaySession: FlowSession = initiateFlow(gateway)

        // Expect gateway to sign the transaction
        val fullySignedTransaction = subFlow(CollectSignaturesFlow(partiallySignedTransaction, listOf(gatewaySession)))

        // Record signed transaction to the vault
        subFlow(FinalityFlow(fullySignedTransaction, listOf(gatewaySession)))
    }
}
