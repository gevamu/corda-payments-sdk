/*
 * Copyright 2023 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gevamu.corda.xml.paymentinstruction

import java.time.Instant
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

@XmlType(
    propOrder = [
        "msgId",
        "nbOfTxs",
        "creDtTm",
    ]
)
class GroupHeader(
    @get:XmlElement(name = "MsgId", required = true) var msgId: String,
    @get:XmlElement(name = "NbOfTxs", required = true) var nbOfTxs: String,
    @get:XmlElement(name = "CreDtTm") var creDtTm: XMLGregorianCalendar,
) {
    constructor() : this(
        msgId = "",
        nbOfTxs = "0",
        creDtTm = DatatypeFactory.newInstance().newXMLGregorianCalendar(Instant.now().toString()),
    )

    fun clone(): GroupHeader {
        return GroupHeader(msgId, nbOfTxs, creDtTm.clone() as XMLGregorianCalendar)
    }
}