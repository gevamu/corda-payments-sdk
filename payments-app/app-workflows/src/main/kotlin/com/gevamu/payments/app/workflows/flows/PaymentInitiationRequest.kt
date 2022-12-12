package com.gevamu.payments.app.workflows.flows

import net.corda.core.serialization.CordaSerializable
import java.io.Serializable
import java.math.BigDecimal

@CordaSerializable
data class PaymentInitiationRequest(
    val creditorAccount: String,
    val debtorAccount: String,
    val amount: BigDecimal
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
