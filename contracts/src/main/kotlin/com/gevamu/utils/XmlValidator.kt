package com.gevamu.utils

import com.gevamu.iso20022.schema.xsd.XsdRepository
import java.io.StringReader
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator


class XmlValidator {
    companion object {
        private val customerCreditTransferInitiationSchemaValidator: Validator =
            createValidator(XsdRepository.getCustomerCreditTransferInitiationSchema())

        private fun createValidator(schemaSource: Source): Validator{
            return SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
                .newSchema(schemaSource)
                .newValidator()
        }
        private fun getRequestPayload(xmlRequest: String, xmlns: String): String{
            val payload = xmlRequest
                .substringAfter("<RequestPayload>")
                .substringBefore("</RequestPayload>")
            return "<Document xmlns=\"$xmlns\">$payload</Document>"
        }
        fun validateCustomerCreditTransferInitiationRequest(xmlRequest: String) {
            val payload = getRequestPayload(xmlRequest, "urn:iso:std:iso:20022:tech:xsd:pain.001.001.11")
            val source = StreamSource(StringReader(payload))
            customerCreditTransferInitiationSchemaValidator.validate(source)
        }
    }
}
