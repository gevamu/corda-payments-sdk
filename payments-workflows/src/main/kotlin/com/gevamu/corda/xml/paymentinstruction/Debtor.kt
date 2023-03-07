package com.gevamu.corda.xml.paymentinstruction

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType


@XmlType(
    propOrder = [
        "name",
        "orgId"
    ]
)
class Debtor(
    @get:XmlElement(name = "Name") var name: String,
    @get:XmlElement(name = "OrgId") var orgId: String
) {
    constructor() : this("", "")
}
