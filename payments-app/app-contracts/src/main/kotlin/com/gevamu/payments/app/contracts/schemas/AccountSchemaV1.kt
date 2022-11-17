package com.gevamu.payments.app.contracts.schemas

import java.io.Serializable
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
import net.corda.core.schemas.MappedSchema
import net.corda.core.serialization.CordaSerializable

object AccountSchema

@CordaSerializable
class AccountSchemaV1 : MappedSchema(
    schemaFamily = AccountSchema::class.java,
    mappedTypes = listOf(Account::class.java, Debtor::class.java, Creditor::class.java, Currency::class.java, Country::class.java, Registration::class.java),
    version = 1
), Serializable {

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

    companion object {
        private const val serialVersionUID = 1L;
    }
}
