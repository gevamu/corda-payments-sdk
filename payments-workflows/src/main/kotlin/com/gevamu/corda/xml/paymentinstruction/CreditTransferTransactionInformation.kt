package com.gevamu.corda.xml.paymentinstruction

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(
    propOrder = [
        "pmtId"
    ]
)
class CreditTransferTransactionInformation(
    @get:XmlElement(name = "PmtId") var pmtId: PaymentIdentification
) {
    constructor() : this(PaymentIdentification())
}