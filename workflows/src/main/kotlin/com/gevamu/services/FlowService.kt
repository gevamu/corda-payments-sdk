// Copyright 2022 Exactpro Systems Limited
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
