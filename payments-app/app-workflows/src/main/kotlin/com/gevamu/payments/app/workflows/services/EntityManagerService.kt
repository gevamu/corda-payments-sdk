package com.gevamu.payments.app.workflows.services

import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class EntityManagerService(
    private val serviceHub: AppServiceHub
) : SingletonSerializeAsToken() {

    fun getCreditor(account: String): AccountSchemaV1.Creditor = serviceHub.withEntityManager {
        find(AccountSchemaV1.Creditor::class.java, account) ?:
        throw NoSuchElementException("Creditor not found $account")
    }

    fun getDebtor(account: String): AccountSchemaV1.Debtor = serviceHub.withEntityManager {
        find(AccountSchemaV1.Debtor::class.java, account) ?:
        throw NoSuchElementException("Debtor not found $account")
    }

    fun getCurrency(isoCode: String): AccountSchemaV1.Currency = serviceHub.withEntityManager {
        find(AccountSchemaV1.Currency::class.java, isoCode) ?:
        throw NoSuchElementException("Currency not found $isoCode")
    }
}
