package com.gevamu.states

import com.gevamu.contracts.PaymentContract
import com.gevamu.schema.PaymentSchemaV1
import java.time.Instant
import java.util.UUID
import net.corda.core.contracts.BelongsToContract
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.node.services.AttachmentId
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable

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
