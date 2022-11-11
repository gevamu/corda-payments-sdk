package com.gevamu.states

import com.gevamu.contracts.PaymentContract
import java.time.Instant
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.node.services.AttachmentId
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(PaymentContract::class)
data class Payment(
    val payer: Party,
    val gateway: Party,
    val paymentInstructionId: AttachmentId,
    val status: PaymentStatus,
    /**
     * Timestamp to record when payment state was proposed/changed.
     * [Instant.now()] by default
     */
    val timestamp: Instant = Instant.now(),
    /**
     * Payment info provided by the bank.
     * Null by default since initial payment doesn't have any bank response
     */
    val additionalInfo: String? = null,
    override val linearId: UniqueIdentifier,
) : LinearState {
    override val participants: List<AbstractParty>
        get() = if (status == PaymentStatus.CREATED) listOf(payer) else listOf(payer, gateway)

    @CordaSerializable
    enum class PaymentStatus {
        CREATED,
        SENT_TO_GATEWAY,
        ACCEPTED,
        PENDING,
        COMPLETED,
        REJECTED
    }
}
