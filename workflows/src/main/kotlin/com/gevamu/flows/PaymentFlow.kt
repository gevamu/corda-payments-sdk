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

/**
 * Combinations of XSD documents describing payments and encoding types (for transformation to ByteArray)
 *
 * @see ByteArray
 */
@CordaSerializable
enum class PaymentInstructionFormat {
    /**
     * pain.001.001.09.xsd + utf-8
     *
     * @see <a href="https://www.iso20022.org/catalogue-messages/iso-20022-messages-archive?search=Payments%20Initiation">
     *     Payments Initiation V10
     *     </a>
     */
    ISO20022_V9_XML_UTF8
}

// Inline link {@link ByteArray byte array} is not rendered in IntelliJ Idea :(
/**
 * Dataclass describing payment instruction
 *
 * @param format Combination of XSD document describing the structure of payment instruction and encoding type
 * @see PaymentInstructionFormat
 *
 * @param paymentInstruction XML document created due to specified XSD
 * and cast to ByteArray in specified encoding
 */
@CordaSerializable
class PaymentInstruction(
    val format: PaymentInstructionFormat,
    val paymentInstruction: ByteArray
)

/**
 * Corda flow for payment initiation
 *
 * @param paymentInstruction Instruction for the payment, you want to execute
 * @see PaymentInstruction
 *
 * @param gateway Identification of the Gevamu Gateway Corda node to process payment
 * @see Party
 *
 * @param uniquePaymentId Identification of the new payment; generated automatically by default
 *
 * @return List with single created payment state
 * @see Payment
 */
@StartableByRPC
class PaymentFlow(
    private val paymentInstruction: PaymentInstruction,
    private val gateway: Party,
    private val uniquePaymentId: UUID = UUID.randomUUID(),
) : FlowLogic<List<StateAndRef<Payment>>>() {

    /**
     * Start main flow logic
     *
     * @return List of created payment states
     */
    @Suspendable
    override fun call(): List<StateAndRef<Payment>> {
        val xmlService = serviceHub.cordaService(XmlService::class.java)
        val flowService = serviceHub.cordaService(FlowService::class.java)

        val paymentRequest: CustomerCreditTransferInitiationV09 =
            xmlService.unmarshalPaymentRequest(paymentInstruction.paymentInstruction)
        val endToEndId: String = paymentRequest.pmtInf.first().cdtTrfTxInf.first().pmtId.endToEndId
        logger.info("Save new payment id=$uniquePaymentId, endToEndId=${endToEndId}")
        val attachmentId = xmlService.storePaymentInstruction(paymentInstruction, ourIdentity)
        // TODO Check participant id

        val payment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = ourIdentity,
            gateway = gateway,
            paymentInstructionId = attachmentId,
            endToEndId = endToEndId,
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
