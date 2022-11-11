package com.gevamu.iso20022.schema

import javax.xml.transform.Source
import javax.xml.transform.sax.SAXSource
import javax.xml.validation.Schema
import javax.xml.validation.Validator
import org.xml.sax.SAXException
import kotlin.jvm.Throws
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.XMLFilterImpl

class XmlValidator(schema: Schema, private val defaultNamespace: String) : XMLFilterImpl() {
    private val validator: Validator = schema.newValidator()

    @Throws(SAXException::class)
    fun validate(xml: String) {
        val xmlSource: Source = SAXSource(this, InputSource(xml.byteInputStream()))
        validator.validate(xmlSource)
    }

    override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
        // Make the validator think the XML file's elements have a namespace
        if (uri.isEmpty()) {
            super.startElement(defaultNamespace, localName, qName, atts)
        } else super.startElement(uri, localName, qName, atts)
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        if (uri.isEmpty()) {
            super.endElement(defaultNamespace, localName, qName)
        } else super.endElement(uri, localName, qName)
    }

    override fun error(excepction: SAXParseException) {
        throw excepction
    }

    override fun warning(excepction: SAXParseException) {
        throw excepction
    }

    override fun fatalError(excepction: SAXParseException) {
        throw excepction
    }

}

