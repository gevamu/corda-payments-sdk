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

package com.gevamu.corda.iso20022

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.Locator
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.ext.Attributes2Impl
import org.xml.sax.helpers.XMLFilterImpl
import org.xml.sax.helpers.XMLReaderFactory
import java.io.IOException
import java.io.InputStream
import java.lang.NullPointerException
import javax.xml.validation.Schema

class Iso20022XmlValidator(private val schema: Schema, private val namespace: String) {
    /**
     * Validate a document. Need to use this method to validate due correct order:
     * 1. XmlValidator
     * 2. SAXValidator
     *
     * In different order will be error because of Document tag,
     * tag will not be passed to SAXValidator and processed as part of xml structure
     *
     * @param inputStream The raw byte stream containing the document.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @exception java.io.IOException An IO exception from the parser,
     *            possibly from a byte stream or character stream
     *            supplied by the application.
     */
    @Throws(SAXException::class, IOException::class)
    fun validate(inputStream: InputStream) {
        val filter = Iso20022XmlFilter(namespace)
        // TODO: implement reader pool for parsing, reader.parse isn't concurrent
        filter.parent = XMLReaderFactory.createXMLReader()
        filter.contentHandler = schema.newValidatorHandler()
        filter.parse(InputSource(inputStream))
    }

    private class Iso20022XmlFilter(private val namespace: String) : XMLFilterImpl() {
        private var namespacePrefix: String? = null

        private var shouldEmitStartDocumentElement: Boolean = true

        private var shouldEmitEndDocumentElement: Boolean = false

        private var locator: Locator? = null

        override fun setDocumentLocator(locator: Locator?) {
            this.locator = locator
            super.setDocumentLocator(locator)
        }

        override fun startPrefixMapping(prefix: String, uri: String) {
            if (namespacePrefix == null && uri == namespace) {
                namespacePrefix = prefix
            }
            super.startPrefixMapping(prefix, uri)
        }

        override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
            if (shouldEmitStartDocumentElement) {
                shouldEmitStartDocumentElement = false
                if (namespacePrefix == null) {
                    reportNoNamespacePrefixError()
                } else {
                    super.startElement(namespace, DOCUMENT_TAG, documentQName, Attributes2Impl())
                    shouldEmitEndDocumentElement = true
                }
            }
            super.startElement(uri, localName, qName, atts)
        }

        override fun endDocument() {
            if (shouldEmitEndDocumentElement) {
                shouldEmitEndDocumentElement = false
                if (namespacePrefix == null) {
                    reportNoNamespacePrefixError()
                } else {
                    super.endElement(namespace, DOCUMENT_TAG, documentQName)
                }
            }
            super.endDocument()
        }

        @Throws(SAXParseException::class)
        override fun error(exception: SAXParseException) {
            throw exception
        }

        @Throws(SAXParseException::class)
        override fun warning(exception: SAXParseException) {
            throw exception
        }

        @Throws(SAXParseException::class)
        override fun fatalError(exception: SAXParseException) {
            throw exception
        }

        private val documentQName: String get() {
            val namespacePrefix = this.namespacePrefix ?: throw NullPointerException("namespacePrefix is null")
            return if (namespacePrefix.isEmpty()) DOCUMENT_TAG else "$namespacePrefix:$DOCUMENT_TAG"
        }

        private fun reportNoNamespacePrefixError() {
            error(SAXParseException("Namespace prefix for $namespace is not set", locator))
        }

        companion object {
            private const val DOCUMENT_TAG = "Document"
        }
    }
}
