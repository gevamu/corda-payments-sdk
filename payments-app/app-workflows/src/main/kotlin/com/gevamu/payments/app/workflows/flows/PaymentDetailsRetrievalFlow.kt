package com.gevamu.payments.app.workflows.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.payments.app.workflows.services.PaymentState
import com.gevamu.payments.app.workflows.services.PaymentStateService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party

@StartableByRPC
class PaymentDetailsRetrievalFlow(
    private val gateway: Party
) : FlowLogic<List<PaymentState>>() {
    @Suspendable
    override fun call(): List<PaymentState> {
        val paymentStateService: PaymentStateService = serviceHub.cordaService(PaymentStateService::class.java)
        return paymentStateService.getPaymentStates()
    }
}
