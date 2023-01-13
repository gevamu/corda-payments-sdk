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
 * 1. hove no more than 1 input ([Payment]);
 * 2. have correct single command ([Commands]).
 *
 * [Commands] verification:
 *
 * - [Commands.Create] should:
 *   1. have required fields ([Payment.endToEndId]);
 *   2. be signed by Participant Node;
 *   3. valid [Payment.PaymentStatus] from the side of workflow.
 *
 * - [Commands.SendToGateway] should:
 *   1. have required fields ([Payment.endToEndId]);
 *   2. be signed by Participant Node;
 *   3. be signed by Gateway Node;
 *   4. have same values in key fields of input and output [Payment]s;
 *   5. valid [Payment.PaymentStatus] from the side of workflow.
 *
 * - [Commands.UpdateStatus] should:
 *   1. have required fields ([Payment.endToEndId]);
 *   2. be signed by Participant Node;
 *   3. be signed by Gateway Node;
 *   4. have same values in key fields of input and output [Payment]s;
 *   5. valid [Payment.PaymentStatus] from the side of workflow.
 *
 * @see Payment
 */
class PaymentContract : Contract {
    companion object {
        const val ID = "com.gevamu.corda.contracts.PaymentContract"
    }

    override fun verify(tx: LedgerTransaction) {
        tx.outputs.forEachIndexed { index, _ ->
            verifyOutput(tx, index)
        }
    }

    private fun verifyOutput(tx: LedgerTransaction, outputIndex: Int) {
        val outputPayment = tx.outputs[outputIndex].data
        if (outputPayment !is Payment) {
            return
        }
        val command = lookupCommand(tx, outputIndex, outputPayment.uniquePaymentId)
        val verifierSpec = VerifierSpec(
            tx = tx,
            output = outputPayment,
            outputIndex = outputIndex,
            command = command,
            input = lookupInput(tx, outputIndex, outputPayment)
        )
        val verifier = when (command.value) {
            is Commands.Create -> CreateVerifier(verifierSpec)
            is Commands.SendToGateway -> SendToGatewayVerifier(verifierSpec)
            is Commands.UpdateStatus -> UpdateStatusVerifier(verifierSpec)
            else -> throw IllegalArgumentException("Unsupported command type ${command.value::class.java}, output index $outputIndex")
        }
        verifier.verify()
    }

    private fun lookupCommand(tx: LedgerTransaction, outputIndex: Int, uniquePaymentId: UUID): CommandWithParties<CommandData> {
        val commands = tx.commands
            .filter {
                if (it.value is Commands) {
                    val command = it.value as Commands
                    command.uniquePaymentId == uniquePaymentId
                } else false
            }
        if (commands.isEmpty()) {
            throw IllegalArgumentException("No commands found for unique payment id $uniquePaymentId, output index $outputIndex")
        }
        if (commands.size > 1) {
            throw IllegalArgumentException("Multiple commands found for unique payment id $uniquePaymentId, output index $outputIndex")
        }
        return commands[0]
    }

    private fun lookupInput(tx: LedgerTransaction, outputIndex: Int, payment: Payment): Payment? {
        val inputs = tx.inputs.map { it.state.data }
            .filterIsInstance<Payment>()
            .filter { it.uniquePaymentId == payment.uniquePaymentId }
        if (inputs.size > 1) {
            throw IllegalArgumentException("Multiple inputs found for unique payment id ${payment.uniquePaymentId}, output index $outputIndex")
        }
        if (inputs.isEmpty()) {
            return null
        }
        return inputs[0]
    }

    class CreateVerifier(
        override val spec: VerifierSpec
    ) : PaymentContractVerifier {
        override fun verify() {
            RequiredFieldsVerifier(spec).verify()
            PayerSignerVerifier(spec).verify()
            PaymentStatusTransitionVerifier(spec).verify()
        }
    }

    class SendToGatewayVerifier(
        override val spec: VerifierSpec
    ) : PaymentContractVerifier {
        override fun verify() {
            RequiredFieldsVerifier(spec).verify()
            PayerSignerVerifier(spec).verify()
            GatewaySignerVerifier(spec).verify()
            PaymentConsensusVerifier(spec).verify()
            PaymentStatusTransitionVerifier(spec).verify()
        }
    }

    class UpdateStatusVerifier(
        override val spec: VerifierSpec
    ) : PaymentContractVerifier {
        override fun verify() {
            RequiredFieldsVerifier(spec).verify()
            PayerSignerVerifier(spec).verify()
            GatewaySignerVerifier(spec).verify()
            PaymentConsensusVerifier(spec).verify()
            PaymentStatusTransitionVerifier(spec).verify()
        }
    }

    class GatewaySignerVerifier(
        override val spec: VerifierSpec
    ) : PaymentContractVerifier {
        override fun verify() {
            if (spec.output == null) {
                throw IllegalArgumentException("The transaction is expected to have an output")
            }
            if (!spec.command.signers.contains(spec.output.gateway.owningKey)) {
                throw IllegalArgumentException("The transaction is not signed by the Gateway (${spec.output.gateway}), output index ${spec.outputIndex}")
            }
        }
    }

    class PayerSignerVerifier(
        override val spec: VerifierSpec
    ) : PaymentContractVerifier {
        override fun verify() {
            if (spec.output == null) {
                throw IllegalArgumentException("The transaction is expected to have an output")
            }
            if (!spec.command.signers.contains(spec.output.payer.owningKey)) {
                throw IllegalArgumentException("The transaction is not signed by the Payer (${spec.output.payer})")
            }
        }
    }

    class RequiredFieldsVerifier(
        override val spec: VerifierSpec
    ) : PaymentContractVerifier {
        override fun verify() {
            if (spec.output == null) {
                throw IllegalArgumentException("The transaction is expected to have an output")
            }
            if (spec.output.endToEndId.isBlank()) {
                throw IllegalArgumentException("The field EndToEndId is blank for unique payment id ${spec.output.uniquePaymentId}, output index ${spec.outputIndex}")
            }
        }
    }

    class PaymentConsensusVerifier(
        override val spec: VerifierSpec
    ) : PaymentContractVerifier {
        override fun verify() {
            if (spec.input == null) {
                throw IllegalArgumentException("The transaction is expected to have an input")
            }
            if (spec.output == null) {
                throw IllegalArgumentException("The transaction is expected to have an output")
            }
            if (spec.input.endToEndId != spec.output.endToEndId) {
                throw IllegalArgumentException("EndToEndId changed for unique payment id ${spec.input.uniquePaymentId}, output index ${spec.outputIndex}")
            }
            if (spec.input.payer != spec.output.payer) {
                throw IllegalArgumentException("Payer changed for unique payment id ${spec.input.uniquePaymentId}, output index ${spec.outputIndex}")
            }
            if (spec.input.gateway != spec.output.gateway) {
                throw IllegalArgumentException("Gateway changed for unique payment id ${spec.input.uniquePaymentId}, output index ${spec.outputIndex}")
            }
        }
    }

    class PaymentStatusTransitionVerifier(
        override val spec: VerifierSpec
    ) : PaymentContractVerifier {
        override fun verify() {
            val isValid = when (spec.command.value) {
                is Commands.Create -> validateCreate()
                is Commands.SendToGateway -> validateSentToGateway()
                is Commands.UpdateStatus -> validateUpdateStatus()
                else -> false
            }
            if (!isValid) {
                throw IllegalArgumentException("Illegal payment status for command ${spec.command.value::class.java.simpleName}, status transition ${spec.input?.status ?: "NONE"} -> ${spec.output?.status ?: "NONE"}")
            }
        }

        private fun validateCreate(): Boolean =
            spec.input == null &&
                spec.output?.status == Payment.PaymentStatus.CREATED

        private fun validateSentToGateway(): Boolean =
            spec.input?.status == Payment.PaymentStatus.CREATED &&
                spec.output?.status == Payment.PaymentStatus.SENT_TO_GATEWAY

        private fun validateUpdateStatus(): Boolean {
            return when (spec.input?.status) {
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
    }

    interface PaymentContractVerifier : Verifier {
        val spec: VerifierSpec
    }

    class VerifierSpec(
        val tx: LedgerTransaction,
        val command: CommandWithParties<CommandData>,
        val outputIndex: Int = -1,
        val output: Payment? = null,
        val input: Payment? = null
    )

    interface Commands : CommandData {
        val uniquePaymentId: UUID
        class Create(override val uniquePaymentId: UUID) : Commands
        class SendToGateway(override val uniquePaymentId: UUID) : Commands
        class UpdateStatus(override val uniquePaymentId: UUID) : Commands
    }
}
