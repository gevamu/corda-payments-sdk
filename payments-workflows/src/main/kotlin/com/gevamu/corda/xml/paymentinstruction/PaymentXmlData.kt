package com.gevamu.corda.xml.paymentinstruction

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "PaymentXmlData")
@XmlType(
    propOrder = [
        "time",
        "pmtInf"
    ]
)
class PaymentXmlData(
    @get:XmlElement(name = "Time") var time: String,
    @get:XmlElement(name = "PaymentInformation") var pmtInf: List<PaymentInformation>
) {
    constructor() : this("", mutableListOf())
}
