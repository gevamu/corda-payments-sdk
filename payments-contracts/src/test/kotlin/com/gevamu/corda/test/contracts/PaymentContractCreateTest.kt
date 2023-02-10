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

package com.gevamu.corda.test.contracts

import com.gevamu.corda.contracts.PaymentContract
import com.gevamu.corda.states.Payment
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class PaymentContractCreateTest : AbstractPaymentContractTest() {
    @Test
    fun `should pass the valid transaction`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.CREATED
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, outputPayment)
                verifies()
            }
        }
    }

    @Test
    fun `should fail if EndToEndId is blank`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = "",
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.CREATED
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, outputPayment)
                failsWith("Field endToEndId cannot be blank (Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if the transaction does not contain Payer's signature`() {
        val thirdParty = createIdentity("Third Party")
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.CREATED
        )

        val command = PaymentContract.Commands.Create(uniquePaymentId)

        ledgerServices.ledger {
            transaction {
                command(thirdParty.publicKey, command)
                output(PaymentContract.ID, outputPayment)
                failsWith("Required signature is absent for command ${command}, index 0, contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if the transaction has the invalid status`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )

        val command = PaymentContract.Commands.Create(uniquePaymentId)

        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, command)
                output(PaymentContract.ID, outputPayment)
                failsWith("Status ${outputPayment.status.name} is not valid for command ${command}, index 0 (Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }
}
