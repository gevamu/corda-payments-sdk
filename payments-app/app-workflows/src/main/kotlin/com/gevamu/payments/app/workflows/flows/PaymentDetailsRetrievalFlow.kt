/*
 * Copyright 2022 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
