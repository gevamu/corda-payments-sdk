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
import net.corda.core.utilities.unwrap


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

        val foo = subFlow(FinalityFlow(fullySignedTransaction, listOf()))

        val newStatus: Payment.PaymentStatus = gatewaySession.receive<Payment.PaymentStatus>().unwrap { it }

        val paymentUpdate = payment.copy(
            status = newStatus,
        )

        val builder2 = TransactionBuilder(notary)
            .addInputState(foo.tx.outRef<Payment>(0))
            .addOutputState(paymentUpdate)
            .addCommand(
                PaymentContract.Commands.UpdateStatus(),
                ourIdentity.owningKey, gateway.owningKey
            )
            .addAttachment(payment.paymentInstructionId)
        builder.verify(serviceHub)

        val partiallySignedTransaction2 = serviceHub.signInitialTransaction(builder2)
        val fullySignedTransaction2 = subFlow(
            CollectSignaturesFlow(partiallySignedTransaction2, listOf(gatewaySession)))

        subFlow(FinalityFlow(fullySignedTransaction2, listOf()))

        return "xxx"
    }
}
