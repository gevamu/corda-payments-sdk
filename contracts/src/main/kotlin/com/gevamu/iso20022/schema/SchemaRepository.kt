package com.gevamu.iso20022.schema

import com.gevamu.iso20022.pain.Document
import javax.xml.transform.stream.StreamSource
import kotlin.jvm.Throws
import java.io.IOException
import javax.xml.XMLConstants
import javax.xml.bind.annotation.XmlSchema
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

object SchemaRepository {
    val CREDIT_TRANSFER_INIT_NAMESPACE by lazy { Document::class.java.`package`.getDeclaredAnnotation(XmlSchema::class.java).namespace }

    private val schemaFactory by lazy {  SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI) }

    @Throws(IOException::class)
    private fun getSource(fileName: String): StreamSource {
        val inputStream = SchemaRepository::class.java.getResourceAsStream(fileName)
        return if (inputStream == null) {
            throw IOException("Resource $fileName wasn't found")
        } else {
            StreamSource(inputStream)
        }
    }

    fun getCustomerCreditTransferInitiationSchema(): Schema {
        val source = getSource("pain.001.001.09.xsd")
        try {
            return schemaFactory.newSchema(source)
        } finally {
            source.inputStream.close()
        }
    }


}
