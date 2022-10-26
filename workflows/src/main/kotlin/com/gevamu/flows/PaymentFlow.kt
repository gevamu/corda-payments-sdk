package com.gevamu.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.contracts.PaymentContract
import com.gevamu.iso20022.pain.CustomerCreditTransferInitiationV09
import com.gevamu.services.FlowService
import com.gevamu.services.XmlService
import com.gevamu.states.Payment
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker


@StartableByRPC
class PaymentFlow(
    private val paymentInstruction: CustomerCreditTransferInitiationV09, private val gateway: Party
) : FlowLogic<UniqueIdentifier>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): UniqueIdentifier {
        val xmlService = serviceHub.cordaService(XmlService::class.java)
        val flowService = serviceHub.cordaService(FlowService::class.java)

        val attachmentId = xmlService.storePaymentInstruction(paymentInstruction, ourIdentity)

        val paymentId = UniqueIdentifier()
        val payment = Payment(
            status = Payment.PaymentStatus.CREATED,
            payer = ourIdentity,
            gateway = gateway,
            paymentInstructionId = attachmentId,
            linearId = paymentId
        )

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary)
            .addOutputState(payment)
            .addCommand(PaymentContract.Commands.Create(), ourIdentity.owningKey)
            .addAttachment(attachmentId)
        builder.verify(serviceHub)

        val signedTransaction = serviceHub.signInitialTransaction(builder)

        subFlow(FinalityFlow(signedTransaction, listOf()))

        flowService.startXxxFlow(paymentId)

        return paymentId
    }
}
