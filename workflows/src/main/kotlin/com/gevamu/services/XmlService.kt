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
import javax.xml.stream.XMLInputFactory

// TODO Exception handling

@CordaService
open class XmlService protected constructor(
    protected val serviceHub: AppServiceHub, xmlClasses: List<Class<*>>
) : SingletonSerializeAsToken() {
    protected val jaxbContext: JAXBContext = JAXBContext.newInstance(
        *(listOf<Class<*>>(CustomerCreditTransferInitiationV09::class.java) + xmlClasses).toTypedArray()
    )

    constructor(serviceHub: AppServiceHub) : this(serviceHub, listOf())

    fun storePaymentInstruction(paymentInstruction: PaymentInstruction, ourIdentity: Party): AttachmentId {
        val zipBytes = zip(listOf(ZipFileEntry("paymentInstruction.xml", paymentInstruction.paymentInstruction)))
        return storeAttachment(zipBytes, ourIdentity)
    }

    fun unmarshalPaymentRequest(bytes: ByteArray): CustomerCreditTransferInitiationV09 {
        val unmarshaller = jaxbContext.createUnmarshaller()
        // XXX store factory in class; there is newDefaultFactory()
        val factory = XMLInputFactory.newFactory()
        val inputStream = ByteArrayInputStream(bytes)
        val jaxbElement = unmarshaller.unmarshal(
            // XXX pass encoding as 2nd argument
            factory.createXMLStreamReader(inputStream),
            CustomerCreditTransferInitiationV09::class.java
        )
        // XXX Do we need to close XML stream reader?
        return jaxbElement.value
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
