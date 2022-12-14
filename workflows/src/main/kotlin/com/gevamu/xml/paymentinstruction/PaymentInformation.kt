package com.gevamu.xml.paymentinstruction

import java.math.BigDecimal
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(
    propOrder = [
        "endToEndId",
        "amount",
        "currency",
        "creditor",
        "debtor"
    ]
)
class PaymentInformation(
    @get:XmlElement(name = "EndToEndId") var endToEndId: String,
    @get:XmlElement(name = "Amount") var amount: BigDecimal,
    @get:XmlElement(name = "Currency") var currency: String,
    @get:XmlElement(name = "Creditor") var creditor: Person,
    @get:XmlElement(name = "Debtor") var debtor: Person
) {
    constructor() : this("", BigDecimal(0), "", Person(), Person())
}
