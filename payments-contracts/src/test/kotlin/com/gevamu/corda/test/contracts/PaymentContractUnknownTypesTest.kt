/*
 * Copyright 2023 Exactpro Systems Limited
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

package com.gevamu.corda.test.contracts

import com.gevamu.corda.contracts.PaymentContract
import com.gevamu.corda.states.Payment
import net.corda.testing.contracts.DummyContract
import net.corda.testing.contracts.DummyState
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class PaymentContractUnknownTypesTest : AbstractPaymentContractTest() {
    @Test
    fun `test unknown state type`() {
        val payment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.CREATED
        )
        val dummy = DummyState()
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, payment)
                command(payer.publicKey, DummyContract.Commands.Create())
                attachment(DummyContract.PROGRAM_ID)
                output(DummyContract.PROGRAM_ID, dummy)
                verifies()
            }
        }
    }
}
