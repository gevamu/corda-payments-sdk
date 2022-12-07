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
    /**
     * Participant Corda node, which initiated payment
     */
    val payer: Party,
    /**
     * Gateway Corda node, which processes payment
     */
    val gateway: Party,
    /**
     * Unique value for the payment in debtor-creditor scope
     */
    val endToEndId: String,
    /**
     * Link to payment instruction, stored as attachment
     *
     * @see AttachmentId
     */
    val paymentInstructionId: AttachmentId,
    /**
     * Status of the payment in workflow
     */
    val status: PaymentStatus,
    /**
     * Payment info provided by the bank.
     *
     * Null by default since initial payment doesn't have any bank response
     */
    val additionalInfo: String? = null,
    /**
     * Unique value for the payment in Corda business network
     */
    val uniquePaymentId: UUID = UUID.randomUUID(),
    /**
     * Timestamp to record when payment state was proposed/changed.
     *
     * Generated with [Instant.now] by default
     */
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
                    timestamp = timestamp
                )
            }
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(PaymentSchemaV1)

    /**
     * Possible payment statuses
     *
     * Workflow:
     * 1. Created
     * 2. Sent to gateway
     * 3. Pending
     * 4. Accepted / Rejected
     *
     * If Accepted:
     * 5. Completed
     */
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
