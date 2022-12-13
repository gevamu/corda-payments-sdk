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

package com.gevamu.payments.app.workflows.services

import com.gevamu.iso20022.pain.CustomerCreditTransferInitiationV09
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.bind.Marshaller
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory

@CordaService
class PaymentInstructionXmlSerializationService(
    private val serviceHub: AppServiceHub
) : SingletonSerializeAsToken() {

    private val jaxbContext: JAXBContext = JAXBContext.newInstance(
        *(listOf<Class<*>>(CustomerCreditTransferInitiationV09::class.java)).toTypedArray()
    )

    fun serialize(paymentInstruction: CustomerCreditTransferInitiationV09): ByteArray {
        val marshaller: Marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        ByteArrayOutputStream().use { output ->
            marshaller.marshal(
                JAXBElement(
                    QName("CstmrCdtTrfInitn"),
                    CustomerCreditTransferInitiationV09::class.java,
                    null,
                    paymentInstruction
                ),
                output
            )
            val result = output.toByteArray()
            if (log.isDebugEnabled) {
                val xml = String(result)
                log.debug("Serialization result:\n{}", xml)
            }
            return result
        }
    }

    fun deserialize(bytes: ByteArray): CustomerCreditTransferInitiationV09 {
        val unmarshaller = jaxbContext.createUnmarshaller()
        val factory = XMLInputFactory.newFactory()
        ByteArrayInputStream(bytes).use { input ->
            val element = unmarshaller.unmarshal(
                factory.createXMLStreamReader(input),
                CustomerCreditTransferInitiationV09::class.java
            )
            return element.value
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(PaymentInstructionXmlSerializationService::class.java)
    }
}
