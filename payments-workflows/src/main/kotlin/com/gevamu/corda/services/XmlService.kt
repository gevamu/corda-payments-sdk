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
import com.gevamu.corda.iso20022.Iso20022XmlValidator
import com.gevamu.corda.xml.paymentinstruction.CustomerCreditTransferInitiation
import net.corda.core.identity.Party
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.AttachmentId
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.transform.Templates
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

// TODO Exception handling
@CordaService
open class XmlService protected constructor(protected val serviceHub: AppServiceHub) : SingletonSerializeAsToken() {
    private val jaxbContext: JAXBContext = JAXBContext.newInstance(CustomerCreditTransferInitiation::class.java)

    private val pain001Validator: Iso20022XmlValidator = Iso20022XmlValidator(pain001Schema(), PAIN_001_NAMESPACE)

    private val pain001Templates: Templates = pain001Xslt()

    fun storePaymentInstruction(paymentInstruction: PaymentInstruction, ourIdentity: Party): AttachmentId {
        // TODO Store format.
        val zipBytes = zip(listOf(ZipFileEntry("paymentInstruction.xml", paymentInstruction.data)))
        return storeAttachment(zipBytes, ourIdentity)
    }

    fun unmarshalPaymentRequest(bytes: ByteArray): CustomerCreditTransferInitiation {
        pain001Validator.validate(bytes.inputStream())

        val streamSource = StreamSource(bytes.inputStream())
        val unmarshallerHandler = jaxbContext.createUnmarshaller().unmarshallerHandler
        val saxResult = SAXResult()
        saxResult.handler = unmarshallerHandler

        // TODO: Create transformer pool
        pain001Templates.newTransformer().transform(streamSource, saxResult)

        return unmarshallerHandler.result as CustomerCreditTransferInitiation
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

    private fun pain001Schema(): Schema = getResourceAsStream("pain.001.001.09.xsd").use {
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(StreamSource(it))
    }

    private fun pain001Xslt(): Templates = getResourceAsStream("pain.001.001.09.xsl").use {
        TransformerFactory.newInstance().newTemplates(StreamSource(it))
    }

    // TODO consider overriding equals and hashCode
    private data class ZipFileEntry(val name: String, val contentBytes: ByteArray)

    companion object {
        const val PAIN_001_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:pain.001.001.09"

        @Throws(IOException::class)
        private fun getResourceAsStream(fileName: String): InputStream {
            return XmlService::class.java.getResourceAsStream(fileName)
                ?: throw IOException("Resource $fileName isn't found")
        }
    }
}
