package com.gevamu.states

import com.gevamu.contracts.PaymentContract
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
    override val linearId: UniqueIdentifier
): LinearState {
    override val participants: List<AbstractParty>
        get() = listOf(payer)

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
