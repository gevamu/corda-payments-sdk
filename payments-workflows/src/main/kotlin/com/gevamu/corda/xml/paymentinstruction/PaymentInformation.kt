package com.gevamu.corda.xml.paymentinstruction

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(
    propOrder = [
        "cdtTrfTxInf",
        "dbtr"
    ]
)
class PaymentInformation(
    @get:XmlElement(name = "CreditTransferTransactionInformation") var cdtTrfTxInf: List<CreditTransferTransactionInformation>,
    @get:XmlElement(name = "Debtor") var dbtr: Debtor
) {
    constructor() : this(emptyList(), Debtor())
}
