package com.gevamu.xsd

import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource

object XsdRepository {
    private fun getSource(fileName: String): StreamSource {
        val inputStream = XsdRepository::class.java.getResourceAsStream(fileName)
        return if (inputStream == null) {
            throw IllegalArgumentException("file not found! $fileName")
        } else {
            StreamSource(inputStream)
        }
    }


    public fun getPain_001_001_11(): Source {
        return getSource("/xsd/pain.001.001.11.xsd")
    }
}
