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
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.Account
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.Debtor
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import java.util.stream.Collectors

@StartableByRPC
class DebtorRetrievalFlow(
    private val gateway: Party
) : FlowLogic<List<Account>>() {
    @Suspendable
    override fun call(): List<Account> = serviceHub.withEntityManager {
        createQuery("FROM Debtor", Debtor::class.java).resultList
            .stream()
            .map(Debtor::account)
            .collect(Collectors.toList())
    } as List<Account>
}
