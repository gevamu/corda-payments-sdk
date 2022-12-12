/*
 * Copyright 2022 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gevamu.payments.app.workflows.services

import com.gevamu.payments.app.contracts.schemas.AppSchemaV1
import com.gevamu.states.Payment.PaymentStatus
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.CordaSerializable
import net.corda.core.serialization.SingletonSerializeAsToken
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.stream.Collectors

@CordaService
class PaymentStateService(
    private val serviceHub: AppServiceHub
) : SingletonSerializeAsToken() {
    fun getPaymentStates(): List<PaymentState> {
        val entityManagerService = serviceHub.cordaService(EntityManagerService::class.java)
        return entityManagerService.getPaymentDetails().stream()
            .map {
                val status = entityManagerService.getPaymentStatus(it.id!!)
                PaymentState(
                    creationTime = it.timestamp!!,
                    paymentId = it.id!!,
                    endToEndId = it.endToEndId!!,
                    amount = it.amount!!,
                    currency = it.currency!!.isoCode!!,
                    creditor = ParticipantAccount.fromCreditor(it.creditor!!),
                    debtor = ParticipantAccount.fromDebtor(it.debtor!!),
                    updateTime = status.timestamp,
                    status = status.status
                )
            }
            .collect(Collectors.toList())
    }
}

@CordaSerializable
data class PaymentState(
    val creationTime: Instant,
    val paymentId: UUID,
    val endToEndId: String,
    val amount: BigDecimal,
    val currency: String,
    val creditor: ParticipantAccount,
    val debtor: ParticipantAccount,
    val updateTime: Instant,
    val status: PaymentStatus
)

@CordaSerializable
data class ParticipantAccount(
    val accountId: String,
    val accountName: String,
    val currency: String
) {

    companion object {
        fun fromDebtor(debtor: AppSchemaV1.Debtor): ParticipantAccount {
            return fromAccount(debtor.account!!)
        }

        fun fromCreditor(creditor: AppSchemaV1.Creditor): ParticipantAccount {
            return fromAccount(creditor.account!!)
        }

        private fun fromAccount(account: AppSchemaV1.Account): ParticipantAccount {
            return ParticipantAccount(
                accountId = account.account!!,
                accountName = account.accountName!!,
                currency = account.currency!!.isoCode!!
            )
        }
    }
}