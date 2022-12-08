// Copyright 2022 Exactpro Systems Limited
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.gevamu.schema

import com.gevamu.states.Payment
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

object PaymentSchema

object PaymentSchemaV1 : MappedSchema(
    schemaFamily = PaymentSchema.javaClass,
    version = 1,
    mappedTypes = listOf(PersistentPayment::class.java),
) {
    /**
     * Class to represent [Payment] state.
     * Used in [net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria]
     * to select StateAndRef for Payment states.
     */
    @Entity
    // TODO add indexes
    @Table(name = "payment_states")
    class PersistentPayment private constructor() : PersistentState() {
        @Column(name = "unique_payment_id", nullable = false)
        lateinit var uniquePaymentId: UUID

        @Column(name = "payer_party", nullable = false)
        lateinit var payer: Party

        @Column(name = "end_to_end_id", length = 35, nullable = false)
        lateinit var endToEndId: String

        @Column(name = "status", nullable = false)
        @Enumerated(EnumType.STRING)
        lateinit var status: Payment.PaymentStatus

        @Column(name = "timestamp", nullable = false)
        lateinit var timestamp: Instant

        constructor(
            uniquePaymentId: UUID,
            payer: Party,
            endToEndId: String,
            status: Payment.PaymentStatus,
            timestamp: Instant
        ) : this() {
            this.uniquePaymentId = uniquePaymentId
            this.payer = payer
            this.endToEndId = endToEndId
            this.status = status
            this.timestamp = timestamp
        }
    }
}
