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
import com.gevamu.corda.xml.paymentinstruction.PaymentXmlData
import net.corda.core.identity.Party
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.AttachmentId
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
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
import javax.xml.transform.Templates
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

    private val customerCreditTransferSchema: Schema = getCustomerCreditTransferInitiationSchema()

    private val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    private val paymentTemplate: Templates = getCCTXslSchemaFile().use {
        TransformerFactory.newInstance().newTemplates(StreamSource(it))
    }

    constructor(serviceHub: AppServiceHub) : this(serviceHub, emptyList())

    fun storePaymentInstruction(paymentInstruction: PaymentInstruction, ourIdentity: Party): AttachmentId {
        val zipBytes = zip(listOf(ZipFileEntry("paymentInstruction.xml", paymentInstruction.paymentInstruction)))
        return storeAttachment(zipBytes, ourIdentity)
    }

    fun unmarshalPaymentRequest(bytes: ByteArray, validate: Boolean = false): PaymentXmlData {
        if (validate) {
            val validator = XmlValidator(customerCreditTransferSchema, CREDIT_TRANSFER_INIT_NAMESPACE)
            //TODO: implement reader pool for parsing, reader.parse isn't concurrent
            validator.parent = XMLReaderFactory.createXMLReader()
            validator.validate(bytes)
        }
        val unmarshaller = jaxbContext.createUnmarshaller()
        val inputStream = StreamSource(bytes.inputStream())

        val outWriter = StringWriter()
        val result = StreamResult(outWriter)

        // TODO: create transformer pool as well, creation of transformer including the parsing and compilation of the XSLT stylesheet
        paymentTemplate.newTransformer().transform(inputStream, result)

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

    private fun getCustomerCreditTransferInitiationSchema(): Schema = getResource("pain.001.001.09.xsd").use {
        schemaFactory.newSchema(StreamSource(it))
    }

    private fun getCCTXslSchemaFile(): InputStream = getResource("pain.001.001.09.xsl")

    companion object {
        const val CREDIT_TRANSFER_INIT_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:pain.001.001.09"

        @Throws(IOException::class)
        private fun getResource(fileName: String): InputStream {
            return XmlService::class.java.getResourceAsStream(fileName)
                ?: throw IOException("Resource $fileName wasn't found")
        }
    }
}
