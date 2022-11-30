package com.gevamu.payments.app.workflows.services

import com.gevamu.payments.app.contracts.schemas.AppSchemaV1
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class EntityManagerService(
    private val serviceHub: AppServiceHub
) : SingletonSerializeAsToken() {

    fun getCreditor(account: String): AppSchemaV1.Creditor = serviceHub.withEntityManager {
        find(AppSchemaV1.Creditor::class.java, account) ?:
        throw NoSuchElementException("Creditor not found $account")
    }

    fun getDebtor(account: String): AppSchemaV1.Debtor = serviceHub.withEntityManager {
        find(AppSchemaV1.Debtor::class.java, account) ?:
        throw NoSuchElementException("Debtor not found $account")
    }

    fun getCurrency(isoCode: String): AppSchemaV1.Currency = serviceHub.withEntityManager {
        find(AppSchemaV1.Currency::class.java, isoCode) ?:
        throw NoSuchElementException("Currency not found $isoCode")
    }
}
