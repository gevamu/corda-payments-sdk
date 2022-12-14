package com.gevamu.xml.paymentinstruction

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "PaymentXmlData")
@XmlType(
    propOrder = [
        "time",
        "paymentInformation"
    ]
)
class PaymentXmlData(
    @get:XmlElement(name = "Time") var time: String,
    @get:XmlElement(name = "PaymentInformation") var paymentInformation: PaymentInformation
) {
    constructor() : this("", PaymentInformation())
}
