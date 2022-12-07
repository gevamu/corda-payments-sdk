package com.gevamu.payments.app.workflows.services

import com.gevamu.payments.app.contracts.schemas.AppSchemaV1
import com.gevamu.schema.PaymentSchemaV1
import java.util.UUID
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

    fun getPaymentDetails(): List<AppSchemaV1.PersistentPaymentDetails> = serviceHub.withEntityManager {
        var query = criteriaBuilder.createQuery(AppSchemaV1.PersistentPaymentDetails::class.java)
        val root = query.from(AppSchemaV1.PersistentPaymentDetails::class.java)
        createQuery(query.select(root)).resultList
    }

    fun getPaymentStatus(id: UUID): PaymentSchemaV1.PersistentPayment = serviceHub.withEntityManager {
        val query = criteriaBuilder.createQuery(PaymentSchemaV1.PersistentPayment::class.java)
        val root = query.from(PaymentSchemaV1.PersistentPayment::class.java)
        val equal = criteriaBuilder.equal(root.get<UUID>("uniquePaymentId"), id)
        createQuery(query.select(root).where(equal))
            .resultList
            .stream()
            .max(Comparator.comparing(PaymentSchemaV1.PersistentPayment::timestamp))
            .orElseThrow {
                NoSuchElementException("No status found for payment $id")
            }
    }
}
