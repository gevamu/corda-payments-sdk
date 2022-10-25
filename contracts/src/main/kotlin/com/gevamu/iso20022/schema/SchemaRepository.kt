package com.gevamu.iso20022.schema

import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource

object SchemaRepository {
    private fun getSource(fileName: String): StreamSource {
        val inputStream = SchemaRepository::class.java.getResourceAsStream(fileName)
        return if (inputStream == null) {
            throw IllegalArgumentException("Resource file not found: $fileName")
        } else {
            StreamSource(inputStream)
        }
    }

    public fun getCustomerCreditTransferInitiationSchema(): Source {
        return getSource("pain.001.001.11.xsd")
    }
}
