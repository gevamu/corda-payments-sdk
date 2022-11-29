package com.gevamu.payments.app.contracts.states

import com.gevamu.payments.app.contracts.contracts.PaymentInitiationContract
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Creditor
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Currency
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Debtor
import com.gevamu.payments.app.contracts.schemas.PaymentDetailsSchemaV1
import com.gevamu.payments.app.contracts.schemas.PaymentDetailsSchemaV1.PersistentPaymentDetails
import java.io.Serializable
import java.math.BigDecimal
import java.time.Instant
import net.corda.core.contracts.BelongsToContract
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(PaymentInitiationContract::class)
class PaymentDetailsState(
    val id: String,
    val paymentDetails: PaymentDetails,
    override val participants: List<AbstractParty>
) : QueryableState {

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is PaymentDetailsSchemaV1 -> PersistentPaymentDetails(id, paymentDetails)
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        TODO("Not yet implemented")
    }
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
