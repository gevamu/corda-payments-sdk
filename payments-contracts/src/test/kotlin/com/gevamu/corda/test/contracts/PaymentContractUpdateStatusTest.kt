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

class PaymentContractUpdateStatusTest : AbstractPaymentContractTest() {
    private val inputPaymentCreate = Payment(
        uniquePaymentId = uniquePaymentId,
        payer = payer.party,
        gateway = gateway.party,
        endToEndId = endToEndId,
        paymentInstructionId = attachmentId,
        status = Payment.PaymentStatus.CREATED
    )

    private val inputPayment = Payment(
        uniquePaymentId = uniquePaymentId,
        payer = payer.party,
        gateway = gateway.party,
        endToEndId = endToEndId,
        paymentInstructionId = attachmentId,
        status = Payment.PaymentStatus.SENT_TO_GATEWAY
    )

    @Test
    fun `test valid payment`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPaymentCreate)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
                output(PaymentContract.ID, "SENT_TO_GATEWAY", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.UpdateStatus(uniquePaymentId))
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, "ACCEPTED", outputPayment)
                verifies()
            }
        }
    }

    @Test
    fun `test missing EndToEndId`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = "",
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPaymentCreate)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
                output(PaymentContract.ID, "SENT_TO_GATEWAY", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.UpdateStatus(uniquePaymentId))
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, "ACCEPTED", outputPayment)
                failsWith("Contract verification failed: The field EndToEndId is blank for unique payment id $uniquePaymentId, output index 0")
            }
        }
    }

    @Test
    fun `test unsigned by Payer`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPaymentCreate)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
                output(PaymentContract.ID, "SENT_TO_GATEWAY", inputPayment)
                verifies()
            }
            transaction {
                command(gateway.publicKey, PaymentContract.Commands.UpdateStatus(uniquePaymentId))
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, "ACCEPTED", outputPayment)
                failsWith("Contract verification failed: The transaction is not signed by the Payer (${payer.party})")
            }
        }
    }

    @Test
    fun `test unsigned by Gateway`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPaymentCreate)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
                output(PaymentContract.ID, "SENT_TO_GATEWAY", inputPayment)
                verifies()
            }
            transaction {
                command(payer.publicKey, PaymentContract.Commands.UpdateStatus(uniquePaymentId))
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, "ACCEPTED", outputPayment)
                failsWith("Contract verification failed: The transaction is not signed by the Gateway (${gateway.party})")
            }
        }
    }

    @Test
    fun `test invalid status`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPaymentCreate)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
                output(PaymentContract.ID, "SENT_TO_GATEWAY", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.UpdateStatus(uniquePaymentId))
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, outputPayment)
                failsWith("Contract verification failed: Illegal payment status for command UpdateStatus, status transition SENT_TO_GATEWAY -> SENT_TO_GATEWAY")
            }
        }
    }

    @Test
    fun `test EndToEnd changed`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = "0",
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPaymentCreate)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
                output(PaymentContract.ID, "SENT_TO_GATEWAY", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.UpdateStatus(uniquePaymentId))
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, "ACCEPTED", outputPayment)
                failsWith("Contract verification failed: EndToEndId changed for unique payment id $uniquePaymentId, output index 0")
            }
        }
    }

    @Test
    fun `test Payer changed`() {
        val thirdParty = createIdentity("Third Party")
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = thirdParty.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPaymentCreate)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
                output(PaymentContract.ID, "SENT_TO_GATEWAY", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(thirdParty.publicKey, gateway.publicKey), PaymentContract.Commands.UpdateStatus(uniquePaymentId))
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, "ACCEPTED", outputPayment)
                failsWith("Contract verification failed: Payer changed for unique payment id $uniquePaymentId, output index 0")
            }
        }
    }

    @Test
    fun `test Gateway changed`() {
        val thirdParty = createIdentity("Third Party")
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = thirdParty.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPaymentCreate)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
                output(PaymentContract.ID, "SENT_TO_GATEWAY", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, thirdParty.publicKey), PaymentContract.Commands.UpdateStatus(uniquePaymentId))
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, "ACCEPTED", outputPayment)
                failsWith("Contract verification failed: Gateway changed for unique payment id $uniquePaymentId, output index 0")
            }
        }
    }
}
