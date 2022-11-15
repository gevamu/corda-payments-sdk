package com.gevamu.payments.app.workflows.services

import com.gevamu.iso20022.pain.CreditTransferTransaction34
import com.gevamu.iso20022.pain.CustomerCreditTransferInitiationV09
import com.gevamu.iso20022.pain.PaymentInstruction30
import com.gevamu.payments.app.contracts.states.ParticipantAccountDetails
import com.gevamu.payments.app.contracts.states.PaymentDetails
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.bind.JAXBContext
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.AttachmentId
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class PaymentInstructionAttachmentService(
    private val serviceHub: AppServiceHub
) : SingletonSerializeAsToken() {

    private val jaxbContext: JAXBContext = JAXBContext.newInstance(
        *(listOf<Class<*>>(CustomerCreditTransferInitiationV09::class.java)).toTypedArray()
    )

    fun getPaymentInstruction(attachmentId: AttachmentId): CustomerCreditTransferInitiationV09 {
        val attachment = serviceHub.attachments.openAttachment(attachmentId)
            ?: throw NoSuchElementException("Attachment not found")
        val unzipped = attachment.open().use {
            unzip(it)
        }
        val paymentInstructionXmlSerializationService = serviceHub.cordaService(PaymentInstructionXmlSerializationService::class.java)
        return paymentInstructionXmlSerializationService.deserialize(unzipped)
    }

    fun getPaymentDetails(attachmentId: AttachmentId): PaymentDetails {
        val entity = getPaymentInstruction(attachmentId)
        return getPaymentDetails(entity)
    }

    fun getPaymentDetails(entity: CustomerCreditTransferInitiationV09): PaymentDetails {
        val creationTime: Instant = entity.grpHdr
            .creDtTm
            .toGregorianCalendar()
            .toZonedDateTime()
            .toInstant()

        if (entity.pmtInf.isNotEmpty()) {
            val paymentInstruction: PaymentInstruction30 = entity.pmtInf[0]
            if (paymentInstruction.cdtTrfTxInf.isNotEmpty()) {
                val transaction = paymentInstruction.cdtTrfTxInf[0]
                val endToEndId = transaction.pmtId.endToEndId
                val amount = transaction.amt.instdAmt.value
                val currency = transaction.amt.instdAmt.ccy
                val creditor = deriveCreditor(transaction)
                val debtor = deriveDebtor(paymentInstruction)

                return PaymentDetails(
                    creationTime = creationTime,
                    endToEndId = endToEndId,
                    amount = amount,
                    currency = currency,
                    creditor = creditor,
                    debtor = debtor
                )
            }
        }

        throw IllegalStateException("Malformed XML attachment")
    }

    private fun deriveCreditor(transaction: CreditTransferTransaction34): ParticipantAccountDetails {
        val creditor = transaction.cdtr.nm
        val creditorAccount = transaction.cdtrAcct.id.othr.id
        val creditorCurrency = transaction.cdtrAcct.ccy
        return ParticipantAccountDetails(
            accountId = creditor,
            accountName = creditorAccount,
            currency = creditorCurrency
        )
    }

    private fun deriveDebtor(paymentInstruction: PaymentInstruction30): ParticipantAccountDetails {
        val debtor = paymentInstruction.dbtr.nm
        val debtorAccount = paymentInstruction.dbtrAcct.id.othr.id
        val debtorCurrency = paymentInstruction.dbtrAcct.ccy
        return ParticipantAccountDetails(
            accountId = debtor,
            accountName = debtorAccount,
            currency = debtorCurrency
        )
    }

    private fun unzip(inputStream: InputStream): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ZipInputStream(inputStream).use { zipInputStream ->
            while (true) {
                val entry: ZipEntry = zipInputStream.nextEntry ?: break
                if (entry.isDirectory || PAYMENT_INSTRUCTION_ATTACHMENT != entry.name) {
                    continue
                }
                val buffer = ByteArray(1024)
                var length: Int
                do {
                    length = zipInputStream.read(buffer)
                    if (length > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                } while (length > 0)
            }
            return outputStream.toByteArray()
        }
    }

    companion object {
        private const val PAYMENT_INSTRUCTION_ATTACHMENT = "paymentInstruction.xml";
    }
}
