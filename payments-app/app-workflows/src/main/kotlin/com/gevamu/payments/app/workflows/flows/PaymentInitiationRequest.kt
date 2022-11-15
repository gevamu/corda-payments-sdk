package com.gevamu.payments.app.workflows.flows

import java.io.Serializable
import java.math.BigDecimal
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class PaymentInitiationRequest(
    val creditorAccount: String,
    val debtorAccount: String,
    val amount: BigDecimal
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
