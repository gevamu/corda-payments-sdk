package com.gevamu.schema

import com.gevamu.states.Payment
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

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

        constructor(
            uniquePaymentId: UUID,
            payer: Party,
            endToEndId: String,
            status: Payment.PaymentStatus,
        ) : this() {
            this.uniquePaymentId = uniquePaymentId
            this.payer = payer
            this.endToEndId = endToEndId
            this.status = status
        }
    }
}