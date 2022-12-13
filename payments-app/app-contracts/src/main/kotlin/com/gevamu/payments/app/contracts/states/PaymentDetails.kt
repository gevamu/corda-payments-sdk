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

package com.gevamu.payments.app.contracts.states

import com.gevamu.payments.app.contracts.contracts.PaymentInitiationContract
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.Creditor
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.Currency
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.Debtor
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.PersistentPaymentDetails
import net.corda.core.contracts.BelongsToContract
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import java.io.Serializable
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@BelongsToContract(PaymentInitiationContract::class)
class PaymentDetailsState(
    val id: UUID,
    val paymentDetails: PaymentDetails,
    override val participants: List<AbstractParty>
) : QueryableState {

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is AppSchemaV1 -> PersistentPaymentDetails(id, paymentDetails)
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(AppSchemaV1)
}

@CordaSerializable
class PaymentDetails(
    var timestamp: Instant,
    var endToEndId: String,
    var amount: BigDecimal,
    var currency: Currency,
    var creditor: Creditor,
    var debtor: Debtor
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
