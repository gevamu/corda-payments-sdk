package com.gevamu.payments.app.contracts.schemas

import com.gevamu.payments.app.contracts.states.PaymentDetails
import java.io.Serializable
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable

object PaymentDetailsSchema

class PaymentDetailsSchemaV1 : MappedSchema(
    schemaFamily = PaymentDetailsSchema::class.java,
    mappedTypes = listOf(PersistentPaymentDetails::class.java),
    version = 1
), Serializable {

    @CordaSerializable
    @Entity(name = "PaymentDetails")
    @Table(name = "payment_details")
    class PersistentPaymentDetails(

        @Id
        @Column(name = "id")
        var id: String? = null,

        @Column(name = "creationTime", nullable = false)
        var creationTime: Instant? = null,

        @Column(name = "endToEndId", nullable = false)
        var endToEndId: String? = null,

        @Column(name = "amount", nullable = false)
        var amount: BigDecimal? = null,

        @OneToOne
        @JoinColumn(name = "currency", referencedColumnName = "isoCode", nullable = false)
        var currency: AccountSchemaV1.Currency? = null,

        @OneToOne
        @JoinColumn(name = "creditor", referencedColumnName = "account")
        var creditor: AccountSchemaV1.Creditor? = null,

        @OneToOne
        @JoinColumn(name = "debtor", referencedColumnName = "account")
        var debtor: AccountSchemaV1.Debtor? = null
    ) : Serializable, PersistentState() {

        constructor(id: String, paymentDetails: PaymentDetails) : this(
            id = id,
            creationTime = paymentDetails.creationTime,
            endToEndId = paymentDetails.endToEndId,
            amount = paymentDetails.amount,
            currency = paymentDetails.currency,
            creditor = paymentDetails.creditor,
            debtor = paymentDetails.debtor
        )

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
