package com.gevamu.iso20022.schema.xsd

import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource

object Repository {
    private fun getSource(fileName: String): StreamSource {
        val inputStream = Repository::class.java.getResourceAsStream(fileName)
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
