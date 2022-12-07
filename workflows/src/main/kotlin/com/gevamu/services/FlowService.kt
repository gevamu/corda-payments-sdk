package com.gevamu.services

import com.gevamu.flows.SendPaymentFlow
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class FlowService(private val serviceHub: AppServiceHub) : SingletonSerializeAsToken() {
    fun startXxxFlow(uniquePaymentId: UUID) {
        // TODO persist execution queue; consider usage of list of Payment states in CREATED status
        executor.execute { serviceHub.startFlow(SendPaymentFlow(uniquePaymentId)) }
    }

    private companion object {
        val executor: Executor = Executors.newFixedThreadPool(8)
    }
}
