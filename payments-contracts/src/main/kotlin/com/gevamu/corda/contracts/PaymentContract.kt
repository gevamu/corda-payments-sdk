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

package com.gevamu.corda.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

/**
 * Payment contract
 */
class PaymentContract : Contract {
    companion object {
        val ID = "com.gevamu.contracts.PaymentContract"
    }

    override fun verify(tx: LedgerTransaction) {
    }

    interface Commands : CommandData {
        class Create : Commands
        class SendToGateway : Commands
        class UpdateStatus : Commands
    }
}
