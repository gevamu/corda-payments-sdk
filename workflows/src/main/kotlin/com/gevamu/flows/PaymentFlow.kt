package com.gevamu.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.contracts.PaymentContract
import com.gevamu.iso20022.pain.CustomerCreditTransferInitiationV09
import com.gevamu.services.FlowService
import com.gevamu.services.XmlService
import com.gevamu.states.Payment
import java.util.UUID
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.TransactionBuilder

@CordaSerializable
enum class PaymentInstructionFormat {
    ISO20022_V9_XML_UTF8
}

@CordaSerializable
class PaymentInstruction(
    val format: PaymentInstructionFormat,
    val paymentInstruction: ByteArray
)

@StartableByRPC
class PaymentFlow(
    private val paymentInstruction: PaymentInstruction, private val gateway: Party,
    private val uniquePaymentId: UUID = UUID.randomUUID(),
) : FlowLogic<List<StateAndRef<Payment>>>() {

    @Suspendable
    override fun call(): List<StateAndRef<Payment>> {
        val xmlService = serviceHub.cordaService(XmlService::class.java)
        val flowService = serviceHub.cordaService(FlowService::class.java)

        val paymentRequest: CustomerCreditTransferInitiationV09 =
            xmlService.unmarshalPaymentRequest(paymentInstruction.paymentInstruction)
        val pmtInfId: String = paymentRequest.pmtInf.first().pmtInfId
        logger.info("Save new payment id=$uniquePaymentId, pmtInfId(paymentId)=${pmtInfId}")
        val attachmentId = xmlService.storePaymentInstruction(paymentInstruction, ourIdentity)
        // TODO Check participant id

        val payment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = ourIdentity,
            gateway = gateway,
            paymentInstructionId = attachmentId,
            paymentId = pmtInfId,
            status = Payment.PaymentStatus.CREATED,
        )

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary)
            .addOutputState(payment)
            .addCommand(PaymentContract.Commands.Create(), ourIdentity.owningKey)
            .addAttachment(attachmentId)
        builder.verify(serviceHub)

        val signedTransaction = serviceHub.signInitialTransaction(builder)

        subFlow(FinalityFlow(signedTransaction, listOf()))

        // TODO move to a service that listens to [PaymentContract.Commands.Create()] command
        logger.info("startXxxFlow id=$uniquePaymentId")
        flowService.startXxxFlow(uniquePaymentId)

        return signedTransaction.tx.filterOutRefs { true }
    }
}
