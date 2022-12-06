// Copyright 2022 Exactpro Systems Limited
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.gevamu.states

import com.gevamu.contracts.PaymentContract
import com.gevamu.schema.PaymentSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.node.services.AttachmentId
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import java.time.Instant
import java.util.UUID

@BelongsToContract(PaymentContract::class)
data class Payment(
    val payer: Party,
    val gateway: Party,
    val endToEndId: String,
    val paymentInstructionId: AttachmentId,
    val status: PaymentStatus,
    /**
     * Payment info provided by the bank.
     * Null by default since initial payment doesn't have any bank response
     */
    val additionalInfo: String? = null,
    /**
     * Timestamp to record when payment state was proposed/changed.
     * [Instant.now()] by default
     */
    val uniquePaymentId: UUID = UUID.randomUUID(),
    val timestamp: Instant = Instant.now(),
) : QueryableState {
    override val participants: List<AbstractParty>
        get() = if (status == PaymentStatus.CREATED) listOf(payer) else listOf(payer, gateway)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is PaymentSchemaV1 -> {
                PaymentSchemaV1.PersistentPayment(
                    uniquePaymentId = uniquePaymentId,
                    payer = payer,
                    endToEndId = endToEndId,
                    status = status,
                )
            }
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(PaymentSchemaV1)

    @CordaSerializable
    enum class PaymentStatus {
        CREATED,
        SENT_TO_GATEWAY,
        ACCEPTED,
        PENDING,
        COMPLETED,
        REJECTED,
    }
}
