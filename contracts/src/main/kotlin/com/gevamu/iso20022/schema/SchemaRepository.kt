package com.gevamu.iso20022.schema

import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import kotlin.jvm.Throws
import java.io.IOException

object SchemaRepository {

    @Throws(IOException::class)
    private fun getSource(fileName: String): StreamSource {
        val inputStream = SchemaRepository::class.java.getResourceAsStream(fileName)
        return if (inputStream == null) {
            throw IOException("Resource $fileName wasn't found")
        } else {
            StreamSource(inputStream)
        }
    }

    fun getCustomerCreditTransferInitiationSchema(): Source {
        return getSource("pain.001.001.11.xsd")
    }
}
