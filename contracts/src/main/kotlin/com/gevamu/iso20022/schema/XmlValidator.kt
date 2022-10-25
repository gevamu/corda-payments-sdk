package com.gevamu.iso20022.schema

import com.gevamu.iso20022.schema.xsd.SchemaRepository
import org.xml.sax.SAXException
import java.io.StringReader
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import kotlin.jvm.Throws

class XmlValidator {
    companion object {
        private val customerCreditTransferInitiationSchema: Schema =
            createSchema(SchemaRepository.getCustomerCreditTransferInitiationSchema())

        private fun createSchema(schemaSource: Source): Schema {
            return SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
                .newSchema(schemaSource)
        }

        @Throws(SAXException::class)
        fun validateCustomerCreditTransferInitiationRequest(xmlRequest: String) {
            val payload = xmlRequest
                .substringAfter("<RequestPayload>")
                .substringBefore("</RequestPayload>")
            val xmlToValidate = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.11\">$payload</Document>"
            val source = StreamSource(StringReader(xmlToValidate))
            customerCreditTransferInitiationSchema.newValidator().validate(source)
        }
    }
}
