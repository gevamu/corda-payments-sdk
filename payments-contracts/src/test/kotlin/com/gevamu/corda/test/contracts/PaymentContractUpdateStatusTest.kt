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
import net.corda.core.crypto.SecureHash
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
                failsWith("Contract verification failed: Field endToEndId cannot be blank (Output state Payment, index 0)")
            }
        }
    }

    @Test
    fun `test absent Payer's signature`() {
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
                verifies()
            }
        }
    }

    @Test
    fun `test absent Gateway's signature`() {
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
                failsWith("Contract verification failed: Required signature absent for command UpdateStatus($uniquePaymentId), index 0")
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
                failsWith("Contract verification failed: Status SENT_TO_GATEWAY is not valid for command UpdateStatus($uniquePaymentId), index 0 (Output state Payment, index 0)")
            }
        }
    }

    @Test
    fun `test EndToEndId changed`() {
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
                failsWith("Contract verification failed: Output state should have same value in endToEndId as input state for command UpdateStatus($uniquePaymentId), index 0 (Input state Payment, index 0; Output state Payment, index 0)")
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
                failsWith("Contract verification failed: Output state should have same value in payer as input state for command UpdateStatus($uniquePaymentId), index 0 (Input state Payment, index 0; Output state Payment, index 0)")
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
                failsWith("Contract verification failed: Output state should have same value in gateway as input state for command UpdateStatus($uniquePaymentId), index 0 (Input state Payment, index 0; Output state Payment, index 0)")
            }
        }
    }

    @Test
    fun `test PaymentInstructionId changed`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = SecureHash.SHA256(ByteArray(32) { 1 }),
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
                failsWith("Contract verification failed: Output state should have same value in paymentInstructionId as input state for command UpdateStatus($uniquePaymentId), index 0 (Input state Payment, index 0; Output state Payment, index 0)")
            }
        }
    }

    @Test
    fun `test status cannot be reverted to CREATED`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )
        val outputPaymentCreated = Payment(
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
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.UpdateStatus(uniquePaymentId))
                input("ACCEPTED")
                output(PaymentContract.ID, "CREATED_AGAIN", outputPaymentCreated)
                failsWith("Contract verification failed: Status CREATED is not valid for command UpdateStatus($uniquePaymentId), index 0 (Output state Payment, index 0)")
            }
        }
    }

    @Test
    fun `test REJECTED status cannot be reverted`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.REJECTED
        )
        val outputPaymentAccepted = Payment(
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
                output(PaymentContract.ID, "REJECTED", outputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.UpdateStatus(uniquePaymentId))
                input("REJECTED")
                output(PaymentContract.ID, "ACCEPTED", outputPaymentAccepted)
                failsWith("Contract verification failed: Status REJECTED is not valid for command UpdateStatus($uniquePaymentId)")
            }
        }
    }
}
