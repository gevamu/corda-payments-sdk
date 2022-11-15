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
    mappedTypes = listOf(Account::class.java, Debtor::class.java, Creditor::class.java, Currency::class.java, Country::class.java),
    version = 1
), Serializable  {

    @CordaSerializable
    @Entity
    @Table(name = "account")
    class Account(

        @Id
        @Column(name = "account")
        var account: String,

        @Column(name = "accountName", nullable = false)
        var accountName: String,

        @Column(name = "bic", nullable = false)
        var bic: String,

        @OneToOne
        @JoinColumn(name = "country", referencedColumnName = "isoCodeAlpha2", nullable = false)
        var country: Country,

        @OneToOne
        @JoinColumn(name = "currency", referencedColumnName = "isoCode", nullable = false)
        var currency: Currency,

        @Column(name = "effectiveDate", nullable = false)
        var effectiveDate: LocalDate,

        @Column(name = "expiryDate", nullable = false)
        var expiryDate: LocalDate,

        @Column(name = "paymentLimit", nullable = false)
        var paymentLimit: Int

    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity
    @Table(name = "debtor")
    class Debtor(
        @Id
        @OneToOne
        @JoinColumn(name = "account", referencedColumnName = "account")
        var account: Account
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity
    @Table(name = "creditor")
    class Creditor(
        @Id
        @OneToOne
        @JoinColumn(name = "account", referencedColumnName = "account")
        var account: Account
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity
    @Table(name = "currency")
    class Currency(
        @Id
        @Column(name = "isoCode", length = 3)
        var isoCode: String
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    @CordaSerializable
    @Entity
    @Table(name = "country")
    class Country(
        @Id
        @Column(name = "isoCodeAlpha2", length = 2)
        var isoCodeAlpha2: String
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    companion object {
        private const val serialVersionUID = 1L;
    }
}
