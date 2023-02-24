package com.gevamu.corda.xml.paymentinstruction

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(
    propOrder = [
        "cdtTrfTxInf"
    ]
)
class PaymentInformation(
    @get:XmlElement(name = "CdtTrfTxInf") var cdtTrfTxInf: List<CreditTransferTransaction>
) {
    constructor() : this(emptyList())
}
