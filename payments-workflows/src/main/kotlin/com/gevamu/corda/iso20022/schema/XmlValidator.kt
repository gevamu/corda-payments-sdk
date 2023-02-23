package com.gevamu.corda.iso20022.schema

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.ext.Attributes2Impl
import org.xml.sax.helpers.XMLFilterImpl
import java.io.IOException
import javax.xml.validation.Schema
import org.xml.sax.helpers.XMLReaderFactory

class XmlValidator(schema: Schema, private val defaultNamespace: String) : XMLFilterImpl() {

    init {
        this.contentHandler = schema.newValidatorHandler()
    }

    /**
     * Validate a document. Need to use this method to validate due correct order:
     * 1. XmlValidator
     * 2. SAXValidator
     *
     * In different order will be error because of Document tag,
     * tag will not be passed to SAXValidator and processed as part of xml structure
     *
     * @param xml data as string, will be wrapped as stream and parsed
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @exception java.io.IOException An IO exception from the parser,
     *            possibly from a byte stream or character stream
     *            supplied by the application.
     */
    @Throws(SAXException::class, IOException::class)
    fun validate(xml: ByteArray) = parse(InputSource(xml.inputStream()))

    override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
        super.startElement(defaultNamespace, localName, qName, atts)
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        super.endElement(defaultNamespace, localName, qName)
    }

    override fun startDocument() {
        // Make the validator think the XML file's elements have a root Document tag
        super.startDocument()
        super.startPrefixMapping("", defaultNamespace)
        super.startElement(defaultNamespace, DOCUMENT_TAG, DOCUMENT_TAG, Attributes2Impl())
    }

    override fun endDocument() {
        super.endElement(defaultNamespace, DOCUMENT_TAG, DOCUMENT_TAG)
        super.endPrefixMapping("")
        super.endDocument()
    }

    @Throws(SAXParseException::class)
    override fun error(excepction: SAXParseException) {
        throw excepction
    }

    @Throws(SAXParseException::class)
    override fun warning(excepction: SAXParseException) {
        throw excepction
    }

    @Throws(SAXParseException::class)
    override fun fatalError(excepction: SAXParseException) {
        throw excepction
    }

    companion object {
        private const val DOCUMENT_TAG = "Document"
    }
}
