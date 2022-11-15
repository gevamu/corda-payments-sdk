package com.gevamu.iso20022.schema

import javax.xml.transform.stream.StreamSource
import kotlin.jvm.Throws
import java.io.IOException

object SchemaRepository {
    const val CREDIT_TRANSFER_INIT_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:pain.001.001.09"

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
