/*******************************************************************************
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
 ******************************************************************************/

package com.gevamu.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.contracts.PaymentContract
import com.gevamu.iso20022.pain.CustomerCreditTransferInitiationV09
import com.gevamu.services.FlowService
import com.gevamu.services.XmlService
import com.gevamu.states.Payment
import com.gevamu.xml.paymentinstruction.PaymentXmlData
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.TransactionBuilder
import java.util.UUID

@CordaSerializable
enum class PaymentInstructionFormat {
    ISO20022_V9_XML_UTF8
}

@CordaSerializable
class PaymentInstruction(
    val format: PaymentInstructionFormat,
    val paymentInstruction: ByteArray
)

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

        val paymentRequest: PaymentXmlData = xmlService.unmarshalPaymentRequest(paymentInstruction.paymentInstruction, true)
        val endToEndId: String = paymentRequest.paymentInformation.endToEndId
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
            .addCommand(PaymentContract.Commands.Create(), ourIdentity.owningKey)
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
