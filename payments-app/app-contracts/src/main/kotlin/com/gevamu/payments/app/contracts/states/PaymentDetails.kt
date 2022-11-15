package com.gevamu.payments.app.contracts.states

import com.gevamu.payments.app.contracts.contracts.PaymentInitiationContract
import java.io.Serializable
import java.math.BigDecimal
import java.time.Instant
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(PaymentInitiationContract::class)
class PaymentDetailsState(
    override val linearId: UniqueIdentifier,
    override val participants: List<AbstractParty>,
    val paymentDetails: PaymentDetails
) : LinearState

@CordaSerializable
class PaymentDetails(
    var creationTime: Instant,
    var endToEndId: String,
    var amount: BigDecimal,
    var currency: String,
    var creditor: ParticipantAccountDetails,
    var debtor: ParticipantAccountDetails
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

@CordaSerializable
class ParticipantAccountDetails(
    var accountId: String,
    var accountName: String,
    var currency: String
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
