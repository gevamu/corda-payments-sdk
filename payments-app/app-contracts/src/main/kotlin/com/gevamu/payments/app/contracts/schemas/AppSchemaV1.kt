package com.gevamu.payments.app.contracts.schemas

import com.gevamu.payments.app.contracts.states.PaymentDetails
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
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable

object AppSchema

@CordaSerializable
object AppSchemaV1 : MappedSchema(
    schemaFamily = AppSchema::class.java,
    mappedTypes = listOf(
        Account::class.java,
        Debtor::class.java,
        Creditor::class.java,
        Currency::class.java,
        Country::class.java,
        Registration::class.java,
        PersistentPaymentDetails::class.java),
    version = 1
), Serializable {

    private const val serialVersionUID = 1L

    @CordaSerializable
    @Entity(name = "Account")
    @Table(name = "account")
    class Account : Serializable {

        @Id
        @Column(name = "account")
        lateinit var account: String

        @Column(name = "account_name", nullable = false)
        lateinit var accountName: String

        @Column(name = "bic", nullable = false)
        lateinit var bic: String

        @OneToOne
        @JoinColumn(name = "country", referencedColumnName = "iso_code_alpha2", nullable = false)
        lateinit var country: Country

        @OneToOne
        @JoinColumn(name = "currency", referencedColumnName = "iso_code", nullable = false)
        lateinit var currency: Currency

        @Column(name = "effective_date", nullable = false)
        lateinit var effectiveDate: LocalDate

        @Column(name = "expiry_date", nullable = false)
        lateinit var expiryDate: LocalDate

        @Column(name = "payment_limit", nullable = false)
        var paymentLimit: Int = 0

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
        lateinit var account: Account

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
        lateinit var account: Account

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
        lateinit var isoCode: String

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
        lateinit var isoCodeAlpha2: String

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
        lateinit var participantId: String

        @Column(name = "network_id", nullable = false)
        lateinit var networkId: String

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @Entity(name = "PaymentDetails")
    @Table(name = "payment_details")
    class PersistentPaymentDetails private constructor() : Serializable, PersistentState() {

        @Column(name = "id", nullable = false)
        lateinit var id: UUID

        @Column(name = "timestamp", nullable = false)
        lateinit var timestamp: Instant

        @Column(name = "end_to_end_id", nullable = false)
        lateinit var endToEndId: String

        @Column(name = "amount", nullable = false)
        lateinit var amount: BigDecimal

        @OneToOne
        @JoinColumn(name = "currency", referencedColumnName = "iso_code", nullable = false)
        lateinit var currency: Currency

        @OneToOne
        @JoinColumn(name = "creditor", referencedColumnName = "account")
        lateinit var creditor: Creditor

        @OneToOne
        @JoinColumn(name = "debtor", referencedColumnName = "account")
        lateinit var debtor: Debtor

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
