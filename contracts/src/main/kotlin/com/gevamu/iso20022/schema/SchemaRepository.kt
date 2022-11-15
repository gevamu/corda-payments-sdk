package com.gevamu.iso20022.schema

import com.gevamu.iso20022.pain.Document
import javax.xml.transform.stream.StreamSource
import kotlin.jvm.Throws
import java.io.IOException
import javax.xml.bind.annotation.XmlSchema

object SchemaRepository {
    val DEFAULT_NAMESPACE by lazy { Document::class.java.`package`.getDeclaredAnnotation(XmlSchema::class.java).namespace }

    @Throws(IOException::class)
    private fun getSource(fileName: String): StreamSource {
        val inputStream = SchemaRepository::class.java.getResourceAsStream(fileName)
        return if (inputStream == null) {
            throw IOException("Resource $fileName wasn't found")
        } else {
            StreamSource(inputStream)
        }
    }

    fun getCustomerCreditTransferInitiationSchema(): StreamSource {
        return getSource("pain.001.001.09.xsd")
    }


}
