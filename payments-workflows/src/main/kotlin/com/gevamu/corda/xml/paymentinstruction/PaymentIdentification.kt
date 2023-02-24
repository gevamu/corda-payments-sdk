package com.gevamu.corda.xml.paymentinstruction

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(
    propOrder = [
        "endToEndId"
    ]
)
class PaymentIdentification(
    @get:XmlElement(name = "EndToEndId") var endToEndId: String
) {
    constructor() : this("")
}
