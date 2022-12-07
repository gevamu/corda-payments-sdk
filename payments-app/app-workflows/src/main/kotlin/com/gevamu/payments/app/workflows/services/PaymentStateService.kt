package com.gevamu.payments.app.workflows.services

import com.gevamu.payments.app.contracts.schemas.AppSchemaV1
import com.gevamu.states.Payment.PaymentStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.stream.Collectors
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.CordaSerializable
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class PaymentStateService(
    private val serviceHub: AppServiceHub
) : SingletonSerializeAsToken() {
    fun getPaymentStates(): List<PaymentState> {
        val entityManagerService = serviceHub.cordaService(EntityManagerService::class.java)
        return entityManagerService.getPaymentDetails().stream()
            .map {
                val paymentState = PaymentState(
                    creationTime = it.timestamp,
                    paymentId = it.id,
                    endToEndId = it.endToEndId,
                    amount = it.amount,
                    currency = it.currency.isoCode,
                    creditor = ParticipantAccount.fromCreditor(it.creditor),
                    debtor = ParticipantAccount.fromDebtor(it.debtor)
                )

                entityManagerService.getPaymentStatus(it.id).ifPresent { status ->
                    paymentState.updateTime = status.timestamp
                    paymentState.status = status.status
                }

                paymentState
            }
            .collect(Collectors.toList());
    }
}

@CordaSerializable
class PaymentState(
    val creationTime: Instant,
    val paymentId: UUID,
    val endToEndId: String,
    val amount: BigDecimal,
    val currency: String,
    val creditor: ParticipantAccount,
    val debtor: ParticipantAccount
) {
    var updateTime: Instant? = null
    var status: PaymentStatus? = null
}

@CordaSerializable
class ParticipantAccount(
    val accountId: String,
    val accountName: String,
    val currency: String
) {

    companion object {
        fun fromDebtor(debtor: AppSchemaV1.Debtor): ParticipantAccount {
            return fromAccount(debtor.account)
        }

        fun fromCreditor(creditor: AppSchemaV1.Creditor): ParticipantAccount {
            return fromAccount(creditor.account)
        }

        private fun fromAccount(account: AppSchemaV1.Account): ParticipantAccount {
            return ParticipantAccount(
                accountId = account.account,
                accountName = account.accountName,
                currency = account.currency.isoCode
            )
        }
    }
}
