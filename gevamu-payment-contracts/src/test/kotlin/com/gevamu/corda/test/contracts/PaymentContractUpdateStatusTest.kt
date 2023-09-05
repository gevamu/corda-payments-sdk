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

package com.gevamu.corda.test.contracts

import com.gevamu.corda.contracts.PaymentContract
import com.gevamu.corda.states.Payment
import net.corda.core.crypto.SecureHash
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test
import java.util.UUID

class PaymentContractUpdateStatusTest : AbstractPaymentContractTest() {
    private val inputPaymentCreate = Payment(
        uniquePaymentId = uniquePaymentId,
        payer = payer.party,
        gateway = gateway.party,
        paymentProviderId = paymentProviderId,
        endToEndId = endToEndId,
        paymentInstructionId = attachmentId,
        status = Payment.PaymentStatus.CREATED
    )

    private val inputPayment = Payment(
        uniquePaymentId = uniquePaymentId,
        payer = payer.party,
        gateway = gateway.party,
        paymentProviderId = paymentProviderId,
        endToEndId = endToEndId,
        paymentInstructionId = attachmentId,
        status = Payment.PaymentStatus.SENT_TO_GATEWAY
    )

    @Test
    fun `should pass valid payment`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
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
                output(PaymentContract.ID, outputPayment)
                verifies()
            }
        }
    }

    @Test
    fun `should fail if endToEndId is blank`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
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
                output(PaymentContract.ID, outputPayment)
                failsWith("Field endToEndId cannot be blank (Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if the transaction does not contain Payer's signature`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )

        val updateStatusCommand = PaymentContract.Commands.UpdateStatus(uniquePaymentId)

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
                command(gateway.publicKey, updateStatusCommand)
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, outputPayment)
                failsWith("Required signature is absent for command $updateStatusCommand, index 0, contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if the transaction does not contain Gateway's signature`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )

        val updateStatusCommand = PaymentContract.Commands.UpdateStatus(uniquePaymentId)

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
                command(payer.publicKey, updateStatusCommand)
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, outputPayment)
                failsWith("Required signature is absent for command $updateStatusCommand, index 0, contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if the transaction has the invalid status`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )

        val updateStatusCommand = PaymentContract.Commands.UpdateStatus(uniquePaymentId)

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
                command(listOf(payer.publicKey, gateway.publicKey), updateStatusCommand)
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, outputPayment)
                failsWith("Status ${outputPayment.status.name} is not valid for command $updateStatusCommand, index 0 (Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if paymentProviderId is unset`() {
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
                output(PaymentContract.ID, outputPayment)
                failsWith("Field paymentProviderId is not set (Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if paymentProviderId is changed`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = UUID.randomUUID(),
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )
        val updateStatusCommand = PaymentContract.Commands.UpdateStatus(uniquePaymentId)
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
                command(listOf(payer.publicKey, gateway.publicKey), updateStatusCommand)
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, outputPayment)
                failsWith("Output state should have same value in paymentProviderId as input state for command $updateStatusCommand, index 0 (Input state ${outputPayment::class.simpleName}, index 0; Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if endToEndId is changed`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = "0",
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )

        val updateStatusCommand = PaymentContract.Commands.UpdateStatus(uniquePaymentId)

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
                command(listOf(payer.publicKey, gateway.publicKey), updateStatusCommand)
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, outputPayment)
                failsWith("Output state should have same value in endToEndId as input state for command $updateStatusCommand, index 0 (Input state ${outputPayment::class.simpleName}, index 0; Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if Payer is changed`() {
        val thirdParty = createIdentity("Third Party")
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = thirdParty.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )

        val updateStatusCommand = PaymentContract.Commands.UpdateStatus(uniquePaymentId)

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
                command(listOf(thirdParty.publicKey, gateway.publicKey), updateStatusCommand)
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, outputPayment)
                failsWith("Output state should have same value in payer as input state for command $updateStatusCommand, index 0 (Input state ${outputPayment::class.simpleName}, index 0; Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if Gateway is changed`() {
        val thirdParty = createIdentity("Third Party")
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = thirdParty.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )

        val updateStatusCommand = PaymentContract.Commands.UpdateStatus(uniquePaymentId)

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
                command(listOf(payer.publicKey, thirdParty.publicKey), updateStatusCommand)
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, outputPayment)
                failsWith("Output state should have same value in gateway as input state for command $updateStatusCommand, index 0 (Input state ${outputPayment::class.simpleName}, index 0; Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if PaymentInstructionId is changed`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = SecureHash.SHA256(ByteArray(32) { 1 }),
            status = Payment.PaymentStatus.ACCEPTED
        )

        val updateStatusCommand = PaymentContract.Commands.UpdateStatus(uniquePaymentId)

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
                command(listOf(payer.publicKey, gateway.publicKey), updateStatusCommand)
                input("SENT_TO_GATEWAY")
                output(PaymentContract.ID, outputPayment)
                failsWith("Output state should have same value in paymentInstructionId as input state for command $updateStatusCommand, index 0 (Input state ${outputPayment::class.simpleName}, index 0; Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if the transaction status is changed to CREATED`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )
        val outputPaymentCreated = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.CREATED
        )

        val updateStatusCommand = PaymentContract.Commands.UpdateStatus(uniquePaymentId)

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
                command(listOf(payer.publicKey, gateway.publicKey), updateStatusCommand)
                input("ACCEPTED")
                output(PaymentContract.ID, outputPaymentCreated)
                failsWith("Status ${outputPaymentCreated.status.name} is not valid for command $updateStatusCommand, index 0 (Output state ${outputPaymentCreated::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if the transaction REJECTED status is changed`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.REJECTED
        )
        val outputPaymentAccepted = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.ACCEPTED
        )

        val updateStatusCommand = PaymentContract.Commands.UpdateStatus(uniquePaymentId)

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
                command(listOf(payer.publicKey, gateway.publicKey), updateStatusCommand)
                input("REJECTED")
                output(PaymentContract.ID, outputPaymentAccepted)
                failsWith("Status ${outputPayment.status.name} is not valid for command $updateStatusCommand, index 0 (Input state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }
}
