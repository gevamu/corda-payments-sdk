package com.gevamu.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.contracts.PaymentContract
import com.gevamu.states.Payment
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker


@InitiatingFlow
@StartableByService
class SendPaymentFlow(private val paymentId: UniqueIdentifier) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val inputCriteria: QueryCriteria.LinearStateQueryCriteria = QueryCriteria.LinearStateQueryCriteria()
            .withUuid(listOf(paymentId.id))
            .withStatus(Vault.StateStatus.UNCONSUMED)
            .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT)
        val paymentStateAndRef
            = serviceHub.vaultService.queryBy(Payment::class.java, inputCriteria).states.first()
        val gateway = paymentStateAndRef.state.data.gateway

        val gatewaySession: FlowSession = initiateFlow(gateway)

        val payment = paymentStateAndRef.state.data.copy(
            status = Payment.PaymentStatus.SENT_TO_GATEWAY,
        )

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary)
            .addInputState(paymentStateAndRef)
            .addOutputState(payment)
            .addCommand(PaymentContract.Commands.SendToGateway(), ourIdentity.owningKey, gateway.owningKey)
            .addAttachment(paymentStateAndRef.state.data.paymentInstructionId)
        builder.verify(serviceHub)

        val partiallySignedTransaction = serviceHub.signInitialTransaction(builder)
        val fullySignedTransaction = subFlow(
            CollectSignaturesFlow(partiallySignedTransaction, listOf(gatewaySession)))

        subFlow(FinalityFlow(fullySignedTransaction, listOf()))

        return "xxx"
    }
}
