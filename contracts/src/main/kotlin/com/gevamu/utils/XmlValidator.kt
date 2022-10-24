package com.gevamu.utils

import com.gevamu.xsd.XsdRepository
import java.io.StringReader
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory


class XmlValidator {
    companion object {
        private fun getRequestPayload(xmlRequest: String): String{
            val payload = xmlRequest
                .substringAfter("<RequestPayload>")
                .substringBefore("</RequestPayload>")
            return "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.11\">$payload</Document>"
        }
        fun validatePain_001_001_11(xmlRequest: String) {
            val source = StreamSource(StringReader(getRequestPayload(xmlRequest)))
            SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
                .newSchema(XsdRepository.getPain_001_001_11())
                .newValidator()
                .validate(source)

            println("XML Request is valid")
        }
    }
}
