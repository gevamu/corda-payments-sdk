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

class PaymentContractSentToGatewayTest : AbstractPaymentContractTest() {
    private val inputPayment = Payment(
        uniquePaymentId = uniquePaymentId,
        payer = payer.party,
        gateway = gateway.party,
        paymentProviderId = paymentProviderId,
        endToEndId = endToEndId,
        paymentInstructionId = attachmentId,
        status = Payment.PaymentStatus.CREATED
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
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
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
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                failsWith("Field endToEndId cannot be blank (Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if paymentProviderId is not set`() {
        val inputPaymentNoProviderId = inputPayment.copy(paymentProviderId = null)
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
                output(PaymentContract.ID, "CREATED", inputPaymentNoProviderId)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                failsWith("Field paymentProviderId is not set (Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should allow setting paymentProviderId if it isn't previously set`() {
        val inputPaymentNoProviderId = inputPayment.copy(paymentProviderId = null)
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPaymentNoProviderId)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), PaymentContract.Commands.SendToGateway(uniquePaymentId))
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                verifies()
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
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )
        val sendToGatewayCommand = PaymentContract.Commands.SendToGateway(uniquePaymentId)
        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), sendToGatewayCommand)
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                failsWith("Field paymentProviderId is not set (Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
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
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )

        val sendToGatewayCommand = PaymentContract.Commands.SendToGateway(uniquePaymentId)

        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPayment)
                verifies()
            }
            transaction {
                command(gateway.publicKey, sendToGatewayCommand)
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                failsWith("Required signature is absent for command $sendToGatewayCommand, index 0, contract: ${PaymentContract.ID}")
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
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )

        val sendToGatewayCommand = PaymentContract.Commands.SendToGateway(uniquePaymentId)

        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPayment)
                verifies()
            }
            transaction {
                command(payer.publicKey, sendToGatewayCommand)
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                failsWith("Required signature is absent for command $sendToGatewayCommand, index 0, contract: ${PaymentContract.ID}")
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
            status = Payment.PaymentStatus.ACCEPTED
        )

        val sendToGatewayCommand = PaymentContract.Commands.SendToGateway(uniquePaymentId)

        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), sendToGatewayCommand)
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                failsWith("Status ${outputPayment.status.name} is not valid for command $sendToGatewayCommand, index 0 (Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
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
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )

        val sendToGatewayCommand = PaymentContract.Commands.SendToGateway(uniquePaymentId)

        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), sendToGatewayCommand)
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                failsWith("Output state should have same value in paymentProviderId as input state for command $sendToGatewayCommand, index 0 (Input state ${outputPayment::class.simpleName}, index 0; Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
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
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )

        val sendToGatewayCommand = PaymentContract.Commands.SendToGateway(uniquePaymentId)

        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), sendToGatewayCommand)
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                failsWith("Output state should have same value in endToEndId as input state for command $sendToGatewayCommand, index 0 (Input state ${outputPayment::class.simpleName}, index 0; Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if payer is changed`() {
        val thirdParty = createIdentity("Third Party")
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = thirdParty.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )

        val sendToGatewayCommand = PaymentContract.Commands.SendToGateway(uniquePaymentId)

        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(thirdParty.publicKey, gateway.publicKey), sendToGatewayCommand)
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                failsWith("Output state should have same value in payer as input state for command $sendToGatewayCommand, index 0 (Input state ${outputPayment::class.simpleName}, index 0; Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if gateway is changed`() {
        val thirdParty = createIdentity("Third Party")
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = thirdParty.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = attachmentId,
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )

        val sendToGatewayCommand = PaymentContract.Commands.SendToGateway(uniquePaymentId)

        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, thirdParty.publicKey), sendToGatewayCommand)
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                failsWith("Output state should have same value in gateway as input state for command $sendToGatewayCommand, index 0 (Input state ${outputPayment::class.simpleName}, index 0; Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }

    @Test
    fun `should fail if paymentInstructionId is changed`() {
        val outputPayment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = payer.party,
            gateway = gateway.party,
            paymentProviderId = paymentProviderId,
            endToEndId = endToEndId,
            paymentInstructionId = SecureHash.SHA256(ByteArray(32) { 1 }),
            status = Payment.PaymentStatus.SENT_TO_GATEWAY
        )

        val sendToGatewayCommand = PaymentContract.Commands.SendToGateway(uniquePaymentId)

        ledgerServices.ledger {
            transaction {
                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
                output(PaymentContract.ID, "CREATED", inputPayment)
                verifies()
            }
            transaction {
                command(listOf(payer.publicKey, gateway.publicKey), sendToGatewayCommand)
                input("CREATED")
                output(PaymentContract.ID, outputPayment)
                failsWith("Output state should have same value in paymentInstructionId as input state for command $sendToGatewayCommand, index 0 (Input state ${outputPayment::class.simpleName}, index 0; Output state ${outputPayment::class.simpleName}, index 0), contract: ${PaymentContract.ID}")
            }
        }
    }
}
