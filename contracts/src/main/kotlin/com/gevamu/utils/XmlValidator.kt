package com.gevamu.utils

import org.xml.sax.SAXException
import java.io.File
import java.io.StringReader
import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory


class XmlValidator {
    companion object {
        fun validate(xmlObject: String): Boolean {
            println("Validation Starts now!")
            //println(Paths.get(XmlValidator::class.java.getResource("").toURI()).toUri())
            //val mySchemaPath: Path = Paths.get(XmlValidator::class.java.getResource("xsd/pain.001.001.11.xsd").toURI())
            //val schemaLocation = File(mySchemaPath.toString())
            // FIXME: Schema location should not be hardcoded
            val schemaLocation  = File("/home/nikolay.dorofeev/Desktop/corda-payments-sdk/contracts/src/main/resources/xsd/pain.001.001.11.xsd")

            val validator = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
                .newSchema(schemaLocation)
                .newValidator()

            val source = StreamSource(StringReader(xmlObject))

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
