/*
 * Copyright 2022-2023 Exactpro Systems Limited
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

package com.gevamu.corda.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.corda.PaymentProviderInfo
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByService
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.unwrap

@CordaSerializable
class GetPaymentProvidersResponse(val providers: List<PaymentProviderInfo>, val serial: Int)

@InitiatingFlow
@StartableByService
class GetPaymentProvidersFlow(private val gateway: Party) : FlowLogic<List<PaymentProviderInfo>>() {
    @Suspendable
    override fun call(): List<PaymentProviderInfo> {
        val gatewaySession = initiateFlow(gateway)
        return gatewaySession
            .receive(GetPaymentProvidersResponse::class.java, maySkipCheckpoint = true)
            .unwrap { response ->
                response.providers.onEach {
                    require(it.javaClass == PaymentProviderInfo::class.java) {
                        "Unexpected PaymentProviderInfo class: ${it.javaClass}"
                    }
                }
            }
    }
}
