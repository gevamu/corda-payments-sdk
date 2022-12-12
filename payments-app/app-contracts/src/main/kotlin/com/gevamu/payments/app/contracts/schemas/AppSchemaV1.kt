package com.gevamu.payments.app.contracts.schemas

import com.gevamu.payments.app.contracts.states.PaymentDetails
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import java.io.Serializable
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

object AppSchema

@CordaSerializable
object AppSchemaV1 :
    MappedSchema(
        schemaFamily = AppSchema::class.java,
        mappedTypes = listOf(
            Account::class.java,
            Debtor::class.java,
            Creditor::class.java,
            Currency::class.java,
            Country::class.java,
            Registration::class.java,
            PersistentPaymentDetails::class.java
        ),
        version = 1
    ),
    Serializable {

    private const val serialVersionUID = 1L

    @CordaSerializable
    @Entity(name = "Account")
    @Table(name = "account")
    class Account : Serializable {

        @Id
        @Column(name = "account")
        var account: String? = null

        @Column(name = "account_name", nullable = false)
        var accountName: String? = null

        @Column(name = "bic", nullable = false)
        var bic: String? = null

        @OneToOne
        @JoinColumn(name = "country", referencedColumnName = "iso_code_alpha2", nullable = false)
        var country: Country? = null

        @OneToOne
        @JoinColumn(name = "currency", referencedColumnName = "iso_code", nullable = false)
        var currency: Currency? = null

        @Column(name = "effective_date", nullable = false)
        var effectiveDate: LocalDate? = null

        @Column(name = "expiry_date", nullable = false)
        var expiryDate: LocalDate? = null

        @Column(name = "payment_limit", nullable = false)
        var paymentLimit: Int? = 0

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity(name = "Debtor")
    @Table(name = "debtor")
    class Debtor : Serializable {

        @Id
        @OneToOne
        @JoinColumn(name = "account", referencedColumnName = "account")
        var account: Account? = null

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity(name = "Creditor")
    @Table(name = "creditor")
    class Creditor : Serializable {

        @Id
        @OneToOne
        @JoinColumn(name = "account", referencedColumnName = "account")
        var account: Account? = null

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity(name = "Currency")
    @Table(name = "currency")
    class Currency : Serializable {

        @Id
        @Column(name = "iso_code", length = 3)
        var isoCode: String? = null

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity(name = "Country")
    @Table(name = "country")
    class Country : Serializable {

        @Id
        @Column(name = "iso_code_alpha2", length = 2)
        var isoCodeAlpha2: String? = null

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity(name = "Registration")
    @Table(name = "registration")
    class Registration : Serializable {

        @Id
        @Column(name = "participant_id")
        var participantId: String? = null

        @Column(name = "network_id", nullable = false)
        var networkId: String? = null

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @Entity(name = "PaymentDetails")
    @Table(name = "payment_details")
    class PersistentPaymentDetails private constructor() : Serializable, PersistentState() {

        @Column(name = "id", nullable = false)
        var id: UUID? = null

        @Column(name = "timestamp", nullable = false)
        var timestamp: Instant? = null

        @Column(name = "end_to_end_id", nullable = false)
        var endToEndId: String? = null

        @Column(name = "amount", nullable = false)
        var amount: BigDecimal? = null

        @OneToOne
        @JoinColumn(name = "currency", referencedColumnName = "iso_code", nullable = false)
        var currency: Currency? = null

        @OneToOne
        @JoinColumn(name = "creditor", referencedColumnName = "account")
        var creditor: Creditor? = null

        @OneToOne
        @JoinColumn(name = "debtor", referencedColumnName = "account")
        var debtor: Debtor? = null

        constructor(id: UUID, paymentDetails: PaymentDetails) : this() {
            this.id = id
            this.timestamp = paymentDetails.timestamp
            this.endToEndId = paymentDetails.endToEndId
            this.amount = paymentDetails.amount
            this.currency = paymentDetails.currency
            this.creditor = paymentDetails.creditor
            this.debtor = paymentDetails.debtor
        }

        companion object {
            private const val serialVersionUID = 1L
        }
    }
}
