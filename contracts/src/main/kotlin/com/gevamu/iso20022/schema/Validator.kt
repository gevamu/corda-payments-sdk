package com.gevamu.iso20022.schema

import com.gevamu.iso20022.schema.xsd.Repository
import org.xml.sax.SAXException
import java.io.StringReader
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import kotlin.jvm.Throws

class Validator {
    companion object {
        private val customerCreditTransferInitiationSchemaValidator: javax.xml.validation.Validator =
            createValidator(Repository.getCustomerCreditTransferInitiationSchema())

        private fun createValidator(schemaSource: Source): javax.xml.validation.Validator {
            return SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
                .newSchema(schemaSource)
                .newValidator()
        }

        @Throws(SAXException::class)
        fun validateCustomerCreditTransferInitiationRequest(xmlRequest: String) {
            val payload = xmlRequest
                .substringAfter("<RequestPayload>")
                .substringBefore("</RequestPayload>")
            val xmlToValidate = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.11\">$payload</Document>"
            val source = StreamSource(StringReader(xmlToValidate))
            customerCreditTransferInitiationSchemaValidator.validate(source)
        }
    }
}
