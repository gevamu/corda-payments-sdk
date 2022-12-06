package com.gevamu.payments.app.contracts.states

import com.gevamu.payments.app.contracts.contracts.PaymentInitiationContract
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.Creditor
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.Currency
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.Debtor
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.PersistentPaymentDetails
import java.io.Serializable
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import net.corda.core.contracts.BelongsToContract
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable

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
    var creationTime: Instant,
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
