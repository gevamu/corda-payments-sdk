package com.gevamu.utils

import com.gevamu.xsd.XsdRepository
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.File
import java.io.InputStream
import java.io.StringReader
import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory


class XmlValidator {
    companion object {
        @Throws(Exception::class)
        private fun loadXMLFromString(xml: String): Document {
            val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val builder: DocumentBuilder = factory.newDocumentBuilder()
            val inputSource = InputSource(StringReader(xml))
            return builder.parse(inputSource)
        }
        private fun getRequestPayload(xmlRequest: String): String{
//            val request: Document = loadXMLFromString(xmlRequest)
//            val payload = request.getElementsByTagName("RequestPayload")
//            println(payload.serialize().toString())
//            return payload.serialize().toString()
            val payload = xmlRequest
                .substringAfter("<RequestPayload>")
                .substringBefore("</RequestPayload>")
            return "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.11\">$payload</Document>"
        }
        fun validate(xmlRequest: String): Boolean {
            println("Validation Starts now!")
            //println(Paths.get(XmlValidator::class.java.getResource("").toURI()).toUri())
//            val classLoader = javaClass.classLoader
//            val inputStream: InputStream = classLoader.getResourceAsStream("xsd/pain.001.001.11.xsd")
//            val mySchemaPath: Path = Paths.get(XmlValidator::class.java.getResource("xsd/pain.001.001.11.xsd").toURI())
//            val schemaLocation = File(mySchemaPath.toString())
            // FIXME: Schema location should not be hardcoded
            // val schemaLocation  = File("/home/nikolay.dorofeev/Desktop/corda-payments-sdk/contracts/src/main/resources/xsd/pain.001.001.11.xsd")

            val validator = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
                .newSchema(XsdRepository.getPain_001_001_11())
                //.newSchema(schemaLocation)
                .newValidator()

            val source = StreamSource(StringReader(getRequestPayload(xmlRequest)))

            val start = System.currentTimeMillis()
            try {
                validator.validate(source)
                println(" XML is valid.")
            } catch (ex: SAXException) {
                println(" XML not valid because " + ex.message)
            }

            println("Validation complete!")

            println("Time (ms): " + (System.currentTimeMillis() - start))
            return true
        }
    }
}
