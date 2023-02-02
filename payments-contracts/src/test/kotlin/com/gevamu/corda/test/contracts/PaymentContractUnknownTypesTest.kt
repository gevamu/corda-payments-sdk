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

// import com.gevamu.corda.contracts.PaymentContract
// import com.gevamu.corda.states.Payment
// import net.corda.core.contracts.BelongsToContract
// import net.corda.core.contracts.ContractState
// import net.corda.core.identity.AbstractParty
// import net.corda.core.identity.Party
// import net.corda.core.node.services.AttachmentId
// import net.corda.testing.node.ledger
// import org.junit.jupiter.api.Test
// import java.time.Instant
// import java.util.UUID

class PaymentContractUnknownTypesTest : AbstractPaymentContractTest() {
//    @BelongsToContract(PaymentContract::class)
//    data class Dummy(
//        val payer: Party,
//        val gateway: Party,
//        val endToEndId: String,
//        val paymentInstructionId: AttachmentId,
//        val status: Payment.PaymentStatus,
//        val additionalInfo: String? = null,
//        val uniquePaymentId: UUID = UUID.randomUUID(),
//        val timestamp: Instant = Instant.now()
//    ) : ContractState {
//        override val participants: List<AbstractParty>
//            get() = if (status == Payment.PaymentStatus.CREATED) listOf(payer) else listOf(payer, gateway)
//    }
//
//    class DummyCommand(override val uniquePaymentId: UUID) : PaymentContract.Commands
//
//    @Test
//    fun `test fails when command type is unknown`() {
//        val dummy = Dummy(
//            uniquePaymentId = uniquePaymentId,
//            payer = payer.party,
//            gateway = gateway.party,
//            endToEndId = endToEndId,
//            paymentInstructionId = attachmentId,
//            status = Payment.PaymentStatus.CREATED
//        )
//        ledgerServices.ledger {
//            transaction {
//                command(payer.publicKey, DummyCommand(uniquePaymentId))
//                output(PaymentContract.ID, dummy)
//                failsWith("Contract verification failed: Unsupported command type ${DummyCommand::class.java}, command index 0")
//            }
//        }
//    }
//
//    @Test
//    fun `test fails when state type is unknown`() {
//        val dummy = Dummy(
//            uniquePaymentId = uniquePaymentId,
//            payer = payer.party,
//            gateway = gateway.party,
//            endToEndId = endToEndId,
//            paymentInstructionId = attachmentId,
//            status = Payment.PaymentStatus.CREATED
//        )
//        ledgerServices.ledger {
//            transaction {
//                command(payer.publicKey, PaymentContract.Commands.Create(uniquePaymentId))
//                output(PaymentContract.ID, dummy)
//                failsWith("Contract verification failed: The transaction is expected to have an output")
//            }
//        }
//    }
}
