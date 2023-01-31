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

import com.gevamu.corda.contracts.PaymentContract.Commands
import com.gevamu.corda.states.Payment
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.CommandWithParties
import net.corda.core.contracts.Contract
import net.corda.core.internal.Verifier
import net.corda.core.transactions.LedgerTransaction
import java.util.UUID

/**
 * Payment contract
 *
 * Verifies that [LedgerTransaction]s connected with [Payment]s are completed correctly.
 *
 * Each output from [LedgerTransaction.outputs] should:
 * 1. have no more than 1 input ([Payment]);
 * 2. have correct single command ([Commands]).
 *
 * [Commands] verification:
 *
 * - [Commands.Create] should:
 *   1. have required fields set ([Payment.endToEndId]);
 *   2. be signed by Participant Node;
 *   3. have valid [Payment.PaymentStatus] from the side of workflow.
 *
 * - [Commands.SendToGateway] should:
 *   1. have required fields set ([Payment.endToEndId]);
 *   2. be signed by Participant Node;
 *   3. be signed by Gateway Node;
 *   4. have same values of key fields of input and output [Payment]s;
 *   5. have valid [Payment.PaymentStatus] from the side of workflow.
 *
 * - [Commands.UpdateStatus] should:
 *   1. have required fields set ([Payment.endToEndId]);
 *   2. be signed by Participant Node;
 *   3. be signed by Gateway Node;
 *   4. have same values of key fields of input and output [Payment]s;
 *   5. have valid [Payment.PaymentStatus] from the side of workflow.
 *
 * @see Payment
 */
class PaymentContract : Contract {
    companion object {
        val ID = "com.gevamu.corda.contracts.PaymentContract"

        private fun String.appendOutputIndex(outputIndex: Int): String =
            "$this, output index $outputIndex"

        private fun String.appendCommandIndex(commandIndex: Int): String =
            "$this, command index $commandIndex"
    }

    override fun verify(tx: LedgerTransaction) {
        tx.commands.filter { it.value is Commands }
            .forEachIndexed { commandIndex, command ->
                val uniquePaymentId = (command.value as Commands).uniquePaymentId
                val verifierSpec = VerifierSpec(
                    command = CommandWithIndex(command, commandIndex),
                    input = lookupInput(tx, uniquePaymentId, commandIndex),
                    output = lookupOutput(tx, uniquePaymentId, commandIndex)
                )
                val verifier = when (command.value) {
                    is Commands.Create -> CreateVerifier(verifierSpec)
                    is Commands.SendToGateway -> SendToGatewayVerifier(verifierSpec)
                    is Commands.UpdateStatus -> UpdateStatusVerifier(verifierSpec)
                    else -> throw IllegalArgumentException(
                        "Unsupported command type ${command.value::class.java}".appendCommandIndex(commandIndex)
                    )
                }
                verifier.verify()
            }
    }

    private fun lookupInput(tx: LedgerTransaction, uniquePaymentId: UUID, commandIndex: Int): PaymentWithIndex? {
        val inputs = tx.inputs
            .map { it.state.data }
            .filterIsInstance<Payment>()
            .filter { it.uniquePaymentId == uniquePaymentId }
            .mapIndexed { index, payment -> PaymentWithIndex(payment, index) }
        require(inputs.size <= 1) {
            "Multiple inputs found for unique payment id $uniquePaymentId".appendCommandIndex(commandIndex)
        }
        if (inputs.isEmpty()) {
            return null
        }
        return inputs.first()
    }

    private fun lookupOutput(tx: LedgerTransaction, uniquePaymentId: UUID, commandIndex: Int): PaymentWithIndex? {
        val outputs = tx.outputs
            .map { it.data }
            .filterIsInstance<Payment>()
            .filter { it.uniquePaymentId == uniquePaymentId }
            .mapIndexed { index, payment -> PaymentWithIndex(payment, index) }
        require(outputs.size <= 1) {
            "Multiple outputs found for unique payment id $uniquePaymentId".appendCommandIndex(commandIndex)
        }
        if (outputs.isEmpty()) {
            return null
        }
        return outputs.first()
    }

    private class CreateVerifier(spec: VerifierSpec) : TopLevelPaymentContractVerifier(spec) {
        override fun doVerifySpec() {
            require(spec.input == null) {
                "The transaction is expected to have no inputs"
            }
            require(spec.output != null) {
                "The transaction is expected to have an output"
            }
        }
        override fun doVerify() {
            RequiredFieldsVerifier(spec).verify()
            PayerSignerVerifier(spec).verify()
        }
        override fun isPaymentStatusTransitionValid(): Boolean =
            spec.input == null && spec.output?.payment?.status == Payment.PaymentStatus.CREATED
    }

    private class SendToGatewayVerifier(spec: VerifierSpec) : TopLevelPaymentContractVerifier(spec) {
        override fun doVerify() {
            RequiredFieldsVerifier(spec).verify()
            PayerSignerVerifier(spec).verify()
            GatewaySignerVerifier(spec).verify()
            PaymentConsensusVerifier(spec).verify()
        }
        override fun isPaymentStatusTransitionValid(): Boolean =
            spec.input?.payment?.status == Payment.PaymentStatus.CREATED && (
                spec.output?.payment?.status == Payment.PaymentStatus.SENT_TO_GATEWAY ||
                    spec.output?.payment?.status == Payment.PaymentStatus.COMPLETED
                )
    }

    private class UpdateStatusVerifier(spec: VerifierSpec) : TopLevelPaymentContractVerifier(spec) {
        override fun doVerify() {
            RequiredFieldsVerifier(spec).verify()
            PayerSignerVerifier(spec).verify()
            GatewaySignerVerifier(spec).verify()
            PaymentConsensusVerifier(spec).verify()
        }

        override fun isPaymentStatusTransitionValid(): Boolean =
            when (spec.input?.payment?.status) {
                Payment.PaymentStatus.SENT_TO_GATEWAY ->
                    spec.output == null ||
                        spec.output.payment.status == Payment.PaymentStatus.ACCEPTED ||
                        spec.output.payment.status == Payment.PaymentStatus.REJECTED ||
                        spec.output.payment.status == Payment.PaymentStatus.PENDING
                Payment.PaymentStatus.ACCEPTED ->
                    spec.output == null ||
                        spec.output.payment.status == Payment.PaymentStatus.ACCEPTED ||
                        spec.output.payment.status == Payment.PaymentStatus.PENDING ||
                        spec.output.payment.status == Payment.PaymentStatus.COMPLETED
                Payment.PaymentStatus.REJECTED ->
                    spec.output == null ||
                        spec.output.payment.status == Payment.PaymentStatus.REJECTED
                Payment.PaymentStatus.COMPLETED ->
                    spec.output == null ||
                        spec.output.payment.status == Payment.PaymentStatus.COMPLETED
                Payment.PaymentStatus.PENDING ->
                    spec.output == null ||
                        spec.output.payment.status == Payment.PaymentStatus.PENDING ||
                        spec.output.payment.status == Payment.PaymentStatus.ACCEPTED ||
                        spec.output.payment.status == Payment.PaymentStatus.REJECTED ||
                        spec.output.payment.status == Payment.PaymentStatus.COMPLETED
                else -> false
            }
    }

    private class GatewaySignerVerifier(override val spec: VerifierSpec) : PaymentContractVerifier {
        override fun verify() {
            require(spec.output != null) {
                "The transaction is expected to have an output".appendCommandIndex(spec.command.index)
            }
            require(spec.command.commandWithParties.signers.contains(spec.output.payment.gateway.owningKey)) {
                "The transaction is not signed by the Gateway (${spec.output.payment.gateway})"
                    .appendOutputIndex(spec.output.index)
            }
        }
    }

    private class PayerSignerVerifier(override val spec: VerifierSpec) : PaymentContractVerifier {
        override fun verify() {
            require(spec.output != null) {
                "The transaction is expected to have an output".appendCommandIndex(spec.command.index)
            }
            require(spec.command.commandWithParties.signers.contains(spec.output.payment.payer.owningKey)) {
                "The transaction is not signed by the Payer (${spec.output.payment.payer})"
            }
        }
    }

    private class RequiredFieldsVerifier(override val spec: VerifierSpec) : PaymentContractVerifier {
        override fun verify() {
            require(spec.output != null) {
                "The transaction is expected to have an output".appendCommandIndex(spec.command.index)
            }
            require(spec.output.payment.endToEndId.isNotBlank()) {
                "The field EndToEndId is blank for unique payment id ${spec.output.payment.uniquePaymentId}"
                    .appendOutputIndex(spec.output.index)
            }
        }
    }

    private class PaymentConsensusVerifier(override val spec: VerifierSpec) : PaymentContractVerifier {
        override fun verify() {
            require(spec.output != null) {
                "The transaction is expected to have an output".appendCommandIndex(spec.command.index)
            }
            require(spec.input != null) {
                "The transaction has no corresponding input for unique payment id ${spec.output.payment.uniquePaymentId}"
                    .appendOutputIndex(spec.output.index)
            }
            require(spec.input.payment.endToEndId == spec.output.payment.endToEndId) {
                "EndToEndId changed for unique payment id ${spec.input.payment.uniquePaymentId}"
                    .appendOutputIndex(spec.output.index)
            }
            require(spec.input.payment.payer == spec.output.payment.payer) {
                "Payer changed for unique payment id ${spec.input.payment.uniquePaymentId}"
                    .appendOutputIndex(spec.output.index)
            }
            require(spec.input.payment.gateway == spec.output.payment.gateway) {
                "Gateway changed for unique payment id ${spec.input.payment.uniquePaymentId}"
                    .appendOutputIndex(spec.output.index)
            }
            require(spec.input.payment.paymentInstructionId == spec.output.payment.paymentInstructionId) {
                "PaymentInstructionId changed for unique payment id ${spec.input.payment.uniquePaymentId}"
                    .appendOutputIndex(spec.output.index)
            }
        }
    }

    private interface PaymentContractVerifier : Verifier {
        val spec: VerifierSpec
    }

    private class VerifierSpec(
        val command: CommandWithIndex,
        val input: PaymentWithIndex? = null,
        val output: PaymentWithIndex? = null
    )

    private class CommandWithIndex(
        val commandWithParties: CommandWithParties<CommandData>,
        val index: Int
    )

    private class PaymentWithIndex(
        val payment: Payment,
        val index: Int
    )

    private abstract class TopLevelPaymentContractVerifier(override val spec: VerifierSpec) : PaymentContractVerifier {
        final override fun verify() {
            doVerifySpec()
            doVerify()
            doVerifyPaymentStatusTransition()
        }
        protected open fun doVerifySpec() {
            require(spec.input != null) {
                "The transaction is expected to have no inputs"
            }
            require(spec.output != null) {
                "The transaction is expected to have an output"
            }
        }
        protected abstract fun doVerify()
        protected abstract fun isPaymentStatusTransitionValid(): Boolean
        private fun doVerifyPaymentStatusTransition() {
            require(isPaymentStatusTransitionValid()) {
                val commandName = spec.command.commandWithParties.value::class.java.simpleName
                val inputStatus = spec.input?.payment?.status ?: "NONE"
                val outputStatus = spec.output?.payment?.status ?: "NONE"
                "Illegal payment status for command $commandName, status transition $inputStatus -> $outputStatus"
            }
        }
    }

    interface Commands : CommandData {
        val uniquePaymentId: UUID
        class Create(override val uniquePaymentId: UUID) : Commands
        class SendToGateway(override val uniquePaymentId: UUID) : Commands
        class UpdateStatus(override val uniquePaymentId: UUID) : Commands
    }
}
