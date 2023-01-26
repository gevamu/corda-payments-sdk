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
    }

    override fun verify(tx: LedgerTransaction) {
        tx.outputs.forEachIndexed { index, _ ->
            if (tx.outputs[index].data is Payment) {
                verifyOutput(tx, index)
            }
        }
    }

    private fun verifyOutput(tx: LedgerTransaction, outputIndex: Int) {
        val outputPayment = tx.outputs[outputIndex].data as Payment
        val command = lookupCommand(tx, outputIndex, outputPayment.uniquePaymentId)
        val verifierSpec = VerifierSpec(
            output = outputPayment,
            outputIndex = outputIndex,
            command = command,
            input = lookupInput(tx, outputIndex, outputPayment)
        )
        val verifier = when (command.value) {
            is Commands.Create -> CreateVerifier(verifierSpec)
            is Commands.SendToGateway -> SendToGatewayVerifier(verifierSpec)
            is Commands.UpdateStatus -> UpdateStatusVerifier(verifierSpec)
            else -> throw IllegalArgumentException(
                "Unsupported command type ${command.value::class.java}".appendOutputIndex(outputIndex)
            )
        }
        verifier.verify()
    }

    private fun lookupCommand(
        tx: LedgerTransaction,
        outputIndex: Int,
        uniquePaymentId: UUID
    ): CommandWithParties<CommandData> {
        val commands = tx.commands
            .filter {
                val command = it.value
                (command is Commands) && (command.uniquePaymentId == uniquePaymentId)
            }
        require(commands.isNotEmpty()) {
            "No commands found for unique payment id $uniquePaymentId".appendOutputIndex(outputIndex)
        }
        require(commands.size == 1) {
            "Multiple commands found for unique payment id $uniquePaymentId".appendOutputIndex(outputIndex)
        }
        return commands.first()
    }

    private fun lookupInput(tx: LedgerTransaction, outputIndex: Int, payment: Payment): Payment? {
        val inputs = tx.inputs
            .map { it.state.data }
            .filterIsInstance<Payment>()
            .filter { it.uniquePaymentId == payment.uniquePaymentId }
        require(inputs.size <= 1) {
            "Multiple inputs found for unique payment id ${payment.uniquePaymentId}".appendOutputIndex(outputIndex)
        }
        if (inputs.isEmpty()) {
            return null
        }
        return inputs.first()
    }

    class CreateVerifier(spec: VerifierSpec) : TopLevelPaymentContractVerifier(spec) {
        override fun doVerify() {
            RequiredFieldsVerifier(spec).verify()
            PayerSignerVerifier(spec).verify()
        }
        override fun isPaymentStatusTransitionValid(): Boolean =
            spec.input == null && spec.output?.status == Payment.PaymentStatus.CREATED
    }

    class SendToGatewayVerifier(spec: VerifierSpec) : TopLevelPaymentContractVerifier(spec) {
        override fun doVerify() {
            RequiredFieldsVerifier(spec).verify()
            PayerSignerVerifier(spec).verify()
            GatewaySignerVerifier(spec).verify()
            PaymentConsensusVerifier(spec).verify()
        }
        override fun isPaymentStatusTransitionValid(): Boolean =
            spec.input?.status == Payment.PaymentStatus.CREATED &&
                spec.output?.status == Payment.PaymentStatus.SENT_TO_GATEWAY
    }

    class UpdateStatusVerifier(spec: VerifierSpec) : TopLevelPaymentContractVerifier(spec) {
        override fun doVerify() {
            RequiredFieldsVerifier(spec).verify()
            PayerSignerVerifier(spec).verify()
            GatewaySignerVerifier(spec).verify()
            PaymentConsensusVerifier(spec).verify()
        }

        override fun isPaymentStatusTransitionValid(): Boolean =
            when (spec.input?.status) {
                Payment.PaymentStatus.SENT_TO_GATEWAY ->
                    spec.output == null ||
                        spec.output.status == Payment.PaymentStatus.ACCEPTED ||
                        spec.output.status == Payment.PaymentStatus.REJECTED ||
                        spec.output.status == Payment.PaymentStatus.PENDING
                Payment.PaymentStatus.ACCEPTED ->
                    spec.output == null ||
                        spec.output.status == Payment.PaymentStatus.ACCEPTED ||
                        spec.output.status == Payment.PaymentStatus.PENDING
                Payment.PaymentStatus.REJECTED ->
                    spec.output == null ||
                        spec.output.status == Payment.PaymentStatus.REJECTED
                Payment.PaymentStatus.COMPLETED ->
                    spec.output == null ||
                        spec.output.status == Payment.PaymentStatus.COMPLETED
                Payment.PaymentStatus.PENDING ->
                    spec.output?.status == Payment.PaymentStatus.PENDING ||
                        spec.output?.status == Payment.PaymentStatus.ACCEPTED ||
                        spec.output?.status == Payment.PaymentStatus.REJECTED ||
                        spec.output?.status == Payment.PaymentStatus.COMPLETED
                else -> false
            }
    }

    class GatewaySignerVerifier(override val spec: VerifierSpec) : PaymentContractVerifier {
        override fun verify() {
            require(spec.output != null) {
                "The transaction is expected to have an output"
            }
            require(spec.command.signers.contains(spec.output.gateway.owningKey)) {
                "The transaction is not signed by the Gateway (${spec.output.gateway})"
                    .appendOutputIndex(spec.outputIndex)
            }
        }
    }

    class PayerSignerVerifier(override val spec: VerifierSpec) : PaymentContractVerifier {
        override fun verify() {
            require(spec.output != null) {
                "The transaction is expected to have an output"
            }
            require(spec.command.signers.contains(spec.output.payer.owningKey)) {
                "The transaction is not signed by the Payer (${spec.output.payer})"
            }
        }
    }

    class RequiredFieldsVerifier(override val spec: VerifierSpec) : PaymentContractVerifier {
        override fun verify() {
            require(spec.output != null) {
                "The transaction is expected to have an output"
            }
            require(spec.output.endToEndId.isNotBlank()) {
                "The field EndToEndId is blank for unique payment id ${spec.output.uniquePaymentId}"
                    .appendOutputIndex(spec.outputIndex)
            }
        }
    }

    class PaymentConsensusVerifier(override val spec: VerifierSpec) : PaymentContractVerifier {
        override fun verify() {
            require(spec.output != null) {
                "The transaction is expected to have an output"
            }
            require(spec.input != null) {
                "The transaction has no corresponding input for unique payment id ${spec.output.uniquePaymentId}"
                    .appendOutputIndex(spec.outputIndex)
            }
            require(spec.input.endToEndId == spec.output.endToEndId) {
                "EndToEndId changed for unique payment id ${spec.input.uniquePaymentId}"
                    .appendOutputIndex(spec.outputIndex)
            }
            require(spec.input.payer == spec.output.payer) {
                "Payer changed for unique payment id ${spec.input.uniquePaymentId}"
                    .appendOutputIndex(spec.outputIndex)
            }
            require(spec.input.gateway == spec.output.gateway) {
                "Gateway changed for unique payment id ${spec.input.uniquePaymentId}"
                    .appendOutputIndex(spec.outputIndex)
            }
        }
    }

    interface PaymentContractVerifier : Verifier {
        val spec: VerifierSpec
    }

    class VerifierSpec(
        val command: CommandWithParties<CommandData>,
        val outputIndex: Int,
        val output: Payment? = null,
        val input: Payment? = null
    )

    abstract class TopLevelPaymentContractVerifier(override val spec: VerifierSpec) : PaymentContractVerifier {
        final override fun verify() {
            doVerify()
            doVerifyPaymentStatusTransition()
        }
        protected abstract fun doVerify()
        protected abstract fun isPaymentStatusTransitionValid(): Boolean
        private fun doVerifyPaymentStatusTransition() {
            require(isPaymentStatusTransitionValid()) {
                val commandName = spec.command.value::class.java.simpleName
                val inputStatus = spec.input?.status ?: "NONE"
                val outputStatus = spec.output?.status ?: "NONE"
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
