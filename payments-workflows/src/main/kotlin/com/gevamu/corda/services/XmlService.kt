/*
 * Copyright 2022 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gevamu.corda.services

import com.gevamu.corda.flows.PaymentInstruction
import com.gevamu.corda.iso20022.schema.XmlValidator
import com.gevamu.xml.paymentinstruction.PaymentXmlData
import net.corda.core.identity.Party
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.AttachmentId
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import org.xml.sax.XMLReader
import org.xml.sax.helpers.XMLReaderFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.StringWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.stream.XMLInputFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

// TODO Exception handling

@CordaService
open class XmlService protected constructor(
    protected val serviceHub: AppServiceHub,
    xmlClasses: List<Class<*>>
) : SingletonSerializeAsToken() {

    protected val jaxbContext: JAXBContext = JAXBContext.newInstance(
        *(listOf<Class<*>>(PaymentXmlData::class.java) + xmlClasses).toTypedArray()
    )
    protected val xmlInputFactory: XMLInputFactory = XMLInputFactory.newFactory()

    private val creditTransferInitValidator: XmlValidator

    private val schemaFactory by lazy { SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI) }
    private val paymentTransformer: Transformer

    init {
        val reader: XMLReader = XMLReaderFactory.createXMLReader()

        creditTransferInitValidator = XmlValidator(
            getCustomerCreditTransferInitiationSchema(),
            CREDIT_TRANSFER_INIT_NAMESPACE
        ).also {
            it.parent = reader
        }

        val templateFactory = TransformerFactory.newInstance()
        val xslStream = getCCTXslSchemaFile()
        paymentTransformer = templateFactory.newTemplates(StreamSource(xslStream)).newTransformer()
        xslStream.close()
    }

    constructor(serviceHub: AppServiceHub) : this(serviceHub, emptyList())

    fun storePaymentInstruction(paymentInstruction: PaymentInstruction, ourIdentity: Party): AttachmentId {
        val zipBytes = zip(listOf(ZipFileEntry("paymentInstruction.xml", paymentInstruction.paymentInstruction)))
        return storeAttachment(zipBytes, ourIdentity)
    }

    fun unmarshalPaymentRequest(bytes: ByteArray, validate: Boolean = false): PaymentXmlData {
        if (validate) creditTransferInitValidator.validate(bytes)
        val unmarshaller = jaxbContext.createUnmarshaller()
        val inputStream = StreamSource(bytes.inputStream())

        val outWriter = StringWriter()
        val result = StreamResult(outWriter)

        paymentTransformer.transform(inputStream, result)

        val jaxbElement = unmarshaller.unmarshal(
            // XXX pass encoding as 2nd argument
            xmlInputFactory.createXMLStreamReader(outWriter.toString().byteInputStream()),
            PaymentXmlData::class.java
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
            ByteArrayInputStream(bytes),
            ourIdentity.toString(),
            null
        )
    }

    // TODO consider overriding equals and hashCode
    private data class ZipFileEntry(val name: String, val contentBytes: ByteArray)

    private fun getCustomerCreditTransferInitiationSchema(): Schema {
        val source = StreamSource(getResource("pain.001.001.09.xsd"))
        try {
            return schemaFactory.newSchema(source)
        } finally {
            source.inputStream.close()
        }
    }

    private fun getCCTXslSchemaFile(): InputStream {
        return getResource("pain.001.001.09.xsl")
    }

    companion object {
        const val CREDIT_TRANSFER_INIT_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:pain.001.001.09"

        @Throws(IOException::class)
        private fun getResource(fileName: String): InputStream {
            return XmlService::class.java.getResourceAsStream(fileName)
                ?: throw IOException("Resource $fileName wasn't found")
        }
    }
}
