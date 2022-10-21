package com.gevamu.services

import com.gevamu.flows.SendPaymentFlow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@CordaService
class FlowService(private val serviceHub: AppServiceHub) : SingletonSerializeAsToken() {
    fun startXxxFlow(paymentId: UniqueIdentifier) {
        // TODO persist execution queue; consider usage of list of Payment states in CREATED status
        executor.execute { serviceHub.startFlow(SendPaymentFlow(paymentId)) }
    }

    private companion object {
        val executor: Executor = Executors.newFixedThreadPool(8)
    }
}
