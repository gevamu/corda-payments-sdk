package com.gevamu.xml.paymentinstruction

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(
    propOrder = [
        "accountName",
        "accountId",
        "currency",
        "orgId"
    ]
)
class Person(
    @get:XmlElement(name = "AccountName") var accountName: String,
    @get:XmlElement(name = "AccountId") var accountId: String,
    @get:XmlElement(name = "Currency") var currency: String,
    @get:XmlElement(name = "OrgId") var orgId: String?,
) {
    constructor() : this("", "", "", null)
}
