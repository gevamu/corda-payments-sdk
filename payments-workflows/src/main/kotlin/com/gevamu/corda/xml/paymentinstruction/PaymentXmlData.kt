package com.gevamu.corda.xml.paymentinstruction

import com.gevamu.corda.xml.paymentinstruction.PaymentInformation
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
    @get:XmlElement(name = "PmtInf") var pmtInf: List<PaymentInformation>
) {
    constructor() : this("", emptyList())
}
