/*******************************************************************************
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
 ******************************************************************************/

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
    protected val serviceHub: AppServiceHub,
    xmlClasses: List<Class<*>>
) : SingletonSerializeAsToken() {
    protected val jaxbContext: JAXBContext = JAXBContext.newInstance(
        *(listOf<Class<*>>(CustomerCreditTransferInitiationV09::class.java) + xmlClasses).toTypedArray()
    )
    protected val xmlInputFactory: XMLInputFactory = XMLInputFactory.newFactory()
    
    constructor(serviceHub: AppServiceHub) : this(serviceHub, emptyList())

    fun storePaymentInstruction(paymentInstruction: PaymentInstruction, ourIdentity: Party): AttachmentId {
        val zipBytes = zip(listOf(ZipFileEntry("paymentInstruction.xml", paymentInstruction.paymentInstruction)))
        return storeAttachment(zipBytes, ourIdentity)
    }

    fun unmarshalPaymentRequest(bytes: ByteArray): CustomerCreditTransferInitiationV09 {
        val unmarshaller = jaxbContext.createUnmarshaller()
        // XXX store factory in class; there is newDefaultFactory()
        val inputStream = ByteArrayInputStream(bytes)
        val jaxbElement = unmarshaller.unmarshal(
            // XXX pass encoding as 2nd argument
            xmlInputFactory.createXMLStreamReader(inputStream),
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
            ByteArrayInputStream(bytes),
            ourIdentity.toString(),
            null
        )
    }

    // TODO consider overriding equals and hashCode
    private data class ZipFileEntry(val name: String, val contentBytes: ByteArray)
}
