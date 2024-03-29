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

package com.gevamu.corda.contracts

import com.gevamu.corda.contracts.PaymentContract.Commands
import com.gevamu.corda.states.Payment
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey
import java.util.EnumSet
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
    sealed class Commands(val uniquePaymentId: UUID) : CommandData {
        class Create(uniquePaymentId: UUID) : Commands(uniquePaymentId)
        class SendToGateway(uniquePaymentId: UUID) : Commands(uniquePaymentId)
        class UpdateStatus(uniquePaymentId: UUID) : Commands(uniquePaymentId)

        override fun toString(): String = "${javaClass.simpleName}($uniquePaymentId)"
    }

    override fun verify(tx: LedgerTransaction) {
        // TODO It may be more performant to remove entries from input and outputs copy as
        //      validation proceeds.
        tx.commands
            .filter { it.value is Commands }
            .forEachIndexed { commandIndex, (signers, _, value) ->
                val verifier = when (val command = value as Commands) {
                    is Commands.Create ->
                        CreateVerifier(VerifierSpec(command, signers, commandIndex, tx))
                    is Commands.SendToGateway ->
                        SendToGatewayVerifier(VerifierSpec(command, signers, commandIndex, tx))
                    is Commands.UpdateStatus ->
                        UpdateStatusVerifier(VerifierSpec(command, signers, commandIndex, tx))
                }
                verifier.verify()
            }
        tx.inputPayments().forEach { payment -> requireAssociatedCommand(payment, tx) }
        tx.outputPayments().forEach { payment -> requireAssociatedCommand(payment, tx) }
    }

    private fun requireAssociatedCommand(paymentWithRef: StateWithRef<Payment>, tx: LedgerTransaction) {
        val uniquePaymentId = paymentWithRef.state.uniquePaymentId
        require(
            tx.commands.stream().anyMatch {
                val command = it.value
                command is Commands && command.uniquePaymentId == uniquePaymentId
            }
        ) {
            "There is no command for $paymentWithRef"
        }
    }

    private class CreateVerifier(
        spec: VerifierSpec<Commands.Create>
    ) : CommandVerifier<Commands.Create>(spec) {
        override fun verify() {
            requireNoInput()
            val output = requireSingleOutput()
            requireValidPayment(output)
            requirePaymentStatus(output, Payment.PaymentStatus.CREATED)
            requireSignature(output.state.payer)
        }
    }

    private class SendToGatewayVerifier(
        spec: VerifierSpec<Commands.SendToGateway>
    ) : CommandVerifier<Commands.SendToGateway>(spec) {
        override fun verify() {
            val input = requireSingleInput()
            val output = requireSingleOutput()
            requireValidPayment(output)
            requirePaymentProviderId(output)
            requirePaymentStatus(input, Payment.PaymentStatus.CREATED)
            requirePaymentStatus(output, Payment.PaymentStatus.SENT_TO_GATEWAY)
            requireNoPaymentChange(input, output)
            requireSignature(output.state.payer, output.state.gateway)
        }
    }

    private class UpdateStatusVerifier(
        spec: VerifierSpec<Commands.UpdateStatus>
    ) : CommandVerifier<Commands.UpdateStatus>(spec) {
        override fun verify() {
            val input = requireSingleInput()
            val output = requireSingleOutput()
            requireValidPayment(output)
            requirePaymentProviderId(output)
            requirePaymentStatus(
                input,
                EnumSet.of(
                    Payment.PaymentStatus.SENT_TO_GATEWAY,
                    Payment.PaymentStatus.ACCEPTED,
                    Payment.PaymentStatus.PENDING
                )
            )
            requirePaymentStatus(
                output,
                EnumSet.of(
                    Payment.PaymentStatus.ACCEPTED,
                    Payment.PaymentStatus.PENDING,
                    Payment.PaymentStatus.REJECTED,
                    Payment.PaymentStatus.COMPLETED
                )
            )
            requireNoPaymentChange(input, output)
            requireSignature(output.state.payer, output.state.gateway)
        }
    }

    private class VerifierSpec<T : Commands>(
        val command: T,
        val signers: List<PublicKey>,
        val commandIndex: Int,
        val tx: LedgerTransaction
    )

    private abstract class CommandVerifier<T : Commands>(
        private val command: T,
        private val signers: List<PublicKey>,
        private val commandIndex: Int,
        private val tx: LedgerTransaction
    ) {
        val uniquePaymentId: UUID = command.uniquePaymentId

        constructor(spec: VerifierSpec<T>) : this(spec.command, spec.signers, spec.commandIndex, spec.tx)

        abstract fun verify()

        protected fun requireNoInput() {
            val inputs = tx.inputPayments(uniquePaymentId)
            require(inputs.isEmpty()) {
                "$commandText should have no input"
            }
        }

        protected fun requireSingleInput(): StateWithRef<Payment> {
            val inputs = tx.inputPayments(uniquePaymentId)
            require(inputs.size == 1) {
                "$commandText should have exactly one input"
            }
            return inputs.first()
        }

        protected fun requireSingleOutput(): StateWithRef<Payment> {
            val outputs = tx.outputPayments(uniquePaymentId)
            require(outputs.size == 1) {
                "$commandText should have exactly one output"
            }
            return outputs.first()
        }

        protected fun requireValidPayment(paymentWithRef: StateWithRef<Payment>) {
            require(paymentWithRef.state.endToEndId.isNotBlank()) {
                "Field endToEndId cannot be blank ($paymentWithRef)"
            }
            // XXX Check participants?
        }

        protected fun requirePaymentStatus(paymentWithRef: StateWithRef<Payment>, expectedStatus: Payment.PaymentStatus) =
            requirePaymentStatus(paymentWithRef, EnumSet.of(expectedStatus))

        protected fun requirePaymentStatus(
            paymentWithRef: StateWithRef<Payment>,
            expectedStatuses: EnumSet<Payment.PaymentStatus>
        ) {
            val actualStatus = paymentWithRef.state.status
            require(expectedStatuses.contains(actualStatus)) {
                "Status $actualStatus is not valid for $commandText ($paymentWithRef)"
            }
        }

        protected fun requirePaymentProviderId(paymentWithRef: StateWithRef<Payment>) {
            require(paymentWithRef.state.paymentProviderId != null) {
                "Field paymentProviderId is not set ($paymentWithRef)"
            }
        }

        protected fun requireSignature(vararg party: Party) {
            require(signers.containsAll(party.map { it.owningKey })) {
                "Required signature is absent for $commandText"
            }
        }

        protected fun requireNoPaymentChange(input: StateWithRef<Payment>, output: StateWithRef<Payment>) {
            val inputPayment = input.state
            val outputPayment = output.state
            // Once paymentProviderId is set, it shouldn't be changed.
            require(
                inputPayment.paymentProviderId == outputPayment.paymentProviderId ||
                    inputPayment.paymentProviderId == null
            ) {
                valueChangedText(input, output, "paymentProviderId")
            }
            require(inputPayment.endToEndId == outputPayment.endToEndId) {
                valueChangedText(input, output, "endToEndId")
            }
            require(inputPayment.paymentInstructionId == outputPayment.paymentInstructionId) {
                valueChangedText(input, output, "paymentInstructionId")
            }
            require(inputPayment.gateway == outputPayment.gateway) {
                valueChangedText(input, output, "gateway")
            }
            require(inputPayment.payer == outputPayment.payer) {
                valueChangedText(input, output, "payer")
            }
        }

        private fun valueChangedText(input: StateWithRef<Payment>, output: StateWithRef<Payment>, fieldName: String) =
            "Output state should have same value in $fieldName as input state for $commandText ($input; $output)"

        private val commandText: String get() =
            "command $command, index $commandIndex"
    }

    enum class InputOutput { INPUT, OUTPUT }

    private data class StateWithRef<T : ContractState>(val state: T, val inputOutput: InputOutput, val index: Int) {
        override fun toString(): String = "$inputOutputText state ${state.javaClass.simpleName}, index $index"

        private val inputOutputText: String get() =
            when (inputOutput) { InputOutput.INPUT -> "Input"; InputOutput.OUTPUT -> "Output" }
    }

    companion object {
        val ID = "com.gevamu.corda.contracts.PaymentContract"

        private fun lookupPayments(
            inputOutput: InputOutput,
            states: List<ContractState>,
            uniquePaymentId: UUID? = null
        ): List<StateWithRef<Payment>> {
            return states
                .filterIsInstance<Payment>()
                .run { if (uniquePaymentId == null) this else this.filter { it.uniquePaymentId == uniquePaymentId } }
                .mapIndexed { index, payment -> StateWithRef(payment, inputOutput, index) }
        }

        private fun LedgerTransaction.inputPayments(uniquePaymentId: UUID? = null) =
            lookupPayments(InputOutput.INPUT, inputs.map { it.state.data }, uniquePaymentId)

        private fun LedgerTransaction.outputPayments(uniquePaymentId: UUID? = null) =
            lookupPayments(InputOutput.OUTPUT, outputs.map { it.data }, uniquePaymentId)
    }
}
