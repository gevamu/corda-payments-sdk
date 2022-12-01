package com.gevamu.services

import com.gevamu.flows.PaymentInstruction
import com.gevamu.iso20022.pain.CustomerCreditTransferInitiationV09
import net.corda.core.identity.Party
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.AttachmentId
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.bind.Marshaller
import javax.xml.namespace.QName


// TODO Exception handling

@CordaService
open class XmlService protected constructor(
    protected val serviceHub: AppServiceHub, xmlClasses: List<Class<*>>
) : SingletonSerializeAsToken() {
    protected val isoJaxbContext: JAXBContext = JAXBContext.newInstance(
        *(listOf<Class<*>>(CustomerCreditTransferInitiationV09::class.java) + xmlClasses).toTypedArray()
    )

    constructor(serviceHub: AppServiceHub) : this(serviceHub, listOf())

    fun storePaymentInstruction(paymentInstruction: CustomerCreditTransferInitiationV09, ourIdentity: Party): AttachmentId {
        val zipBytes = zip(listOf(ZipFileEntry("paymentInstruction.xml", toXmlBytes(paymentInstruction))))
        return storeAttachment(zipBytes, ourIdentity)
    }

    fun storePaymentInstruction(paymentInstruction: PaymentInstruction, ourIdentity: Party): AttachmentId {
        val zipBytes = zip(listOf(ZipFileEntry("paymentInstruction.xml", paymentInstruction.paymentInstruction)))
        return storeAttachment(zipBytes, ourIdentity)
    }

    private fun toXmlBytes(paymentInstruction: CustomerCreditTransferInitiationV09): ByteArray {
        val marshaller = isoJaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        val outputStream = ByteArrayOutputStream()
        marshaller.marshal(
            // TODO it may be possible to instruct xjc to add XmlRootElement annotation using bindings file
            JAXBElement(
                QName("CstmrCdtTrfInitn"), CustomerCreditTransferInitiationV09::class.java, null, paymentInstruction
            ),
            // XXX Which character encoding is used?
            outputStream
        )
        return outputStream.toByteArray()
    }

    private fun zip(zipEntries: List<ZipFileEntry>): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { zipStream ->
            for (entry in zipEntries) {
                val zipEntry = ZipEntry(entry.name)
                zipStream.putNextEntry(zipEntry)
                zipStream.write(entry.contentBytes)
            }
        }
        return outputStream.toByteArray()
    }

    private fun storeAttachment(bytes: ByteArray, ourIdentity: Party): AttachmentId {
        return serviceHub.attachments.importAttachment(
            ByteArrayInputStream(bytes), ourIdentity.toString(), null
        )
    }

    // TODO consider overriding equals and hashCode
    private data class ZipFileEntry(val name: String, val contentBytes: ByteArray)
}
