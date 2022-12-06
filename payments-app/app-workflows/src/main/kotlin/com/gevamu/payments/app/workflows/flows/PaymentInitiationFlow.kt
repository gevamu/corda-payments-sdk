package com.gevamu.payments.app.workflows.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.flows.PaymentFlow
import com.gevamu.flows.PaymentInstruction
import com.gevamu.flows.PaymentInstructionFormat
import com.gevamu.payments.app.contracts.contracts.PaymentInitiationContract
import com.gevamu.payments.app.contracts.states.PaymentDetailsState
import com.gevamu.payments.app.workflows.services.PaymentInstructionAttachmentService
import com.gevamu.payments.app.workflows.services.PaymentInstructionBuilderService
import com.gevamu.payments.app.workflows.services.PaymentInstructionXmlSerializationService
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder

@StartableByRPC
class PaymentInitiationFlow(
    private val request: PaymentInitiationRequest,
    private val gateway: Party
) : FlowLogic<List<StateAndRef<PaymentDetailsState>>>() {
    @Suspendable
    override fun call(): List<StateAndRef<PaymentDetailsState>> {
        val paymentInstructionBuilderService = serviceHub.cordaService(PaymentInstructionBuilderService::class.java)
        val paymentInstructionXmlSerializationService = serviceHub.cordaService(PaymentInstructionXmlSerializationService::class.java)
        val paymentInstructionAttachmentService = serviceHub.cordaService(PaymentInstructionAttachmentService::class.java)

        val paymentInstruction = paymentInstructionBuilderService.buildPaymentInstruction(request)
        val bytes = paymentInstructionXmlSerializationService.serialize(paymentInstruction)

        val paymentEnvelope = PaymentInstruction(PaymentInstructionFormat.ISO20022_V9_XML_UTF8, bytes)
        val paymentState = subFlow(PaymentFlow(paymentEnvelope, gateway)).first()
        val payment = paymentState.state.data

        val paymentDetails = paymentInstructionAttachmentService.getPaymentDetails(paymentInstruction)

        val paymentDetailsState = PaymentDetailsState(
            id = payment.uniquePaymentId,
            paymentDetails = paymentDetails,
            participants = payment.participants,
        )

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary)
            .addOutputState(paymentDetailsState)
            .addCommand(PaymentInitiationContract.Commands.Create(), ourIdentity.owningKey)
        builder.verify(serviceHub)

        val signedTransaction = serviceHub.signInitialTransaction(builder)

        subFlow(FinalityFlow(signedTransaction, listOf()))

        return signedTransaction.tx.filterOutRefs { true }
    }
}
