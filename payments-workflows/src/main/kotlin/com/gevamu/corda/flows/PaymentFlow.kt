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

package com.gevamu.corda.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.corda.contracts.PaymentContract
import com.gevamu.corda.iso20022.pain.CustomerCreditTransferInitiationV09
import com.gevamu.corda.services.FlowService
import com.gevamu.corda.services.XmlService
import com.gevamu.corda.states.Payment
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.TransactionBuilder
import java.util.UUID

/**
 * Combinations of XSD documents, describing payments, and encoding types (for cast from [ByteArray])
 */
@CordaSerializable
enum class PaymentInstructionFormat {
    /**
     * pain.001.001.09.xsd + utf-8
     *
     * @see <a href="https://www.iso20022.org/catalogue-messages/iso-20022-messages-archive?search=Payments%20Initiation">
     *     Payments Initiation V10
     *     </a>
     */
    ISO20022_V9_XML_UTF8
}

/**
 * Dataclass describing payment instruction
 *
 * @param format Combination of XSD document describing the structure of payment instruction and encoding type
 * @see PaymentInstructionFormat
 *
 * @param paymentInstruction XML document created due to specified XSD
 * and cast to [ByteArray] in specified encoding
 */
@CordaSerializable
class PaymentInstruction(
    val format: PaymentInstructionFormat,
    val paymentInstruction: ByteArray
)

/**
 * Corda flow for payment initiation
 *
 * @param paymentInstruction Instruction for the payment, you want to execute
 * @see PaymentInstruction
 *
 * @param gateway Identification of the Gevamu Gateway Corda node to process payment
 * @see Party
 *
 * @param uniquePaymentId Identification of the new payment; generated with [UUID.randomUUID] by default
 *
 * @return List with single created [Payment] state
 * @see Payment
 */
@StartableByRPC
class PaymentFlow(
    private val paymentInstruction: PaymentInstruction,
    private val gateway: Party,
    private val uniquePaymentId: UUID = UUID.randomUUID(),
) : FlowLogic<List<StateAndRef<Payment>>>() {

    @Suspendable
    override fun call(): List<StateAndRef<Payment>> {
        val xmlService = serviceHub.cordaService(XmlService::class.java)
        val flowService = serviceHub.cordaService(FlowService::class.java)

        val paymentRequest: CustomerCreditTransferInitiationV09 =
            xmlService.unmarshalPaymentRequest(paymentInstruction.paymentInstruction)
        val endToEndId: String = paymentRequest.pmtInf.first().cdtTrfTxInf.first().pmtId.endToEndId
        logger.info("Save new payment id=$uniquePaymentId, endToEndId=$endToEndId")
        val attachmentId = xmlService.storePaymentInstruction(paymentInstruction, ourIdentity)
        // TODO Check participant id

        val payment = Payment(
            uniquePaymentId = uniquePaymentId,
            payer = ourIdentity,
            gateway = gateway,
            paymentInstructionId = attachmentId,
            endToEndId = endToEndId,
            status = Payment.PaymentStatus.CREATED,
        )

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary)
            .addOutputState(payment)
            .addCommand(PaymentContract.Commands.Create(uniquePaymentId), ourIdentity.owningKey)
            .addAttachment(attachmentId)
        builder.verify(serviceHub)

        val signedTransaction = serviceHub.signInitialTransaction(builder)

        subFlow(FinalityFlow(signedTransaction, listOf()))

        // TODO move to a service that listens to [PaymentContract.Commands.Create()] command
        logger.info("startXxxFlow id=$uniquePaymentId")
        flowService.startXxxFlow(uniquePaymentId)

        return signedTransaction.tx.filterOutRefs { true }
    }
}
