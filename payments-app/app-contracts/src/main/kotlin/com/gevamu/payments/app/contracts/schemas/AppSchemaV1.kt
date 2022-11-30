package com.gevamu.payments.app.contracts.schemas

import com.gevamu.payments.app.contracts.states.PaymentDetails
import java.io.Serializable
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
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
    class Account(

        @Id
        @Column(name = "account")
        var account: String? = null,

        @Column(name = "accountName", nullable = false)
        var accountName: String? = null,

        @Column(name = "bic", nullable = false)
        var bic: String? = null,

        @OneToOne
        @JoinColumn(name = "country", referencedColumnName = "isoCodeAlpha2", nullable = false)
        var country: Country? = null,

        @OneToOne
        @JoinColumn(name = "currency", referencedColumnName = "isoCode", nullable = false)
        var currency: Currency? = null,

        @Column(name = "effectiveDate", nullable = false)
        var effectiveDate: LocalDate? = null,

        @Column(name = "expiryDate", nullable = false)
        var expiryDate: LocalDate? = null,

        @Column(name = "paymentLimit", nullable = false)
        var paymentLimit: Int? = null

    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity(name = "Debtor")
    @Table(name = "debtor")
    class Debtor(
        @Id
        @OneToOne
        @JoinColumn(name = "account", referencedColumnName = "account")
        var account: Account? = null
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity(name = "Creditor")
    @Table(name = "creditor")
    class Creditor(
        @Id
        @OneToOne
        @JoinColumn(name = "account", referencedColumnName = "account")
        var account: Account? = null
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity(name = "Currency")
    @Table(name = "currency")
    class Currency(
        @Id
        @Column(name = "isoCode", length = 3)
        var isoCode: String? = null
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity(name = "Country")
    @Table(name = "country")
    class Country(
        @Id
        @Column(name = "isoCodeAlpha2", length = 2)
        var isoCodeAlpha2: String? = null
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity(name = "Registration")
    @Table(name = "registration")
    class Registration(

        @Id
        @Column(name = "participantId")
        var participantId: String? = null,

        @Column(name = "networkId", nullable = false)
        var networkId: String? = null

    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity(name = "PaymentDetails")
    @Table(name = "payment_details")
    class PersistentPaymentDetails(

        @Column(name = "id", nullable = false)
        var id: String? = null,

        @Column(name = "creationTime", nullable = false)
        var creationTime: Instant? = null,

        @Column(name = "endToEndId", nullable = false)
        var endToEndId: String? = null,

        @Column(name = "amount", nullable = false)
        var amount: BigDecimal? = null,

        @OneToOne
        @JoinColumn(name = "currency", referencedColumnName = "isoCode", nullable = false)
        var currency: Currency? = null,

        @OneToOne
        @JoinColumn(name = "creditor", referencedColumnName = "account")
        var creditor: Creditor? = null,

        @OneToOne
        @JoinColumn(name = "debtor", referencedColumnName = "account")
        var debtor: Debtor? = null
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
}
