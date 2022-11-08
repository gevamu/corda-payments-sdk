package com.gevamu.iso20022.schema

import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.Source
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import kotlin.jvm.Throws

object XmlValidator {
    private val customerCreditTransferInitiationSchema: Schema =
        createSchema(SchemaRepository.getCustomerCreditTransferInitiationSchema())

    private val xmlReader: XMLReader = SAXParserFactory.newInstance().apply {
        isValidating = false
        schema = customerCreditTransferInitiationSchema
    }.newSAXParser().xmlReader

    private fun createSchema(schemaSource: Source): Schema {
        return SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
            .newSchema(schemaSource)
    }

    @Throws(SAXException::class)
    fun validateCustomerCreditTransferInitiationRequest(xmlRequestPayload: String) {
        xmlReader.parse(xmlRequestPayload)
    }
}
