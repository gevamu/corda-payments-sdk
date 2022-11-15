package com.gevamu.payments.app.workflows.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.flows.PaymentFlow
import com.gevamu.flows.PaymentInstruction
import com.gevamu.flows.PaymentInstructionFormat
import com.gevamu.payments.app.contracts.contracts.PaymentInitiationContract
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Creditor
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Debtor
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

        val debtor = getDebtor()
        val creditor = getCreditor()

        val paymentInstruction = paymentInstructionBuilderService.buildPaymentInstruction(request.amount, debtor, creditor)
        val bytes = paymentInstructionXmlSerializationService.serialize(paymentInstruction)

        val paymentEnvelope = PaymentInstruction(PaymentInstructionFormat.ISO20022_V9_XML_UTF8, bytes)
        val paymentState = subFlow(PaymentFlow(paymentEnvelope, gateway)).first()
        val payment = paymentState.state.data

        val paymentDetails = paymentInstructionAttachmentService.getPaymentDetails(paymentInstruction)

        val paymentDetailsState = PaymentDetailsState(
            linearId = payment.linearId,
            participants = payment.participants,
            paymentDetails = paymentDetails
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

    private fun getDebtor(): Debtor = serviceHub.withEntityManager {
        find(Debtor::class.java, request.debtorAccount) ?:
        throw NoSuchElementException("Debtor not found ${request.debtorAccount}")
    }

    private fun getCreditor(): Creditor = serviceHub.withEntityManager {
        find(Creditor::class.java, request.creditorAccount) ?:
        throw NoSuchElementException("Creditor not found ${request.creditorAccount}")
    }
}
