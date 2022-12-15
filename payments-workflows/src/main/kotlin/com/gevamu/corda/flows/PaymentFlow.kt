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
    private val uniquePaymentIdToETEIdMap: Map<String, UUID> = emptyMap(),
) : FlowLogic<List<StateAndRef<Payment>>>() {

    @Suspendable
    override fun call(): List<StateAndRef<Payment>> {
        val xmlService = serviceHub.cordaService(XmlService::class.java)
        val flowService = serviceHub.cordaService(FlowService::class.java)

        val paymentRequest: CustomerCreditTransferInitiationV09 =
            xmlService.unmarshalPaymentRequest(paymentInstruction.paymentInstruction)
        val endToEndIds = paymentRequest.pmtInf.map { it.cdtTrfTxInf.first().pmtId.endToEndId }
        logger.info("Save new payment id=$uniquePaymentIdToETEIdMap, endToEndId=$endToEndIds")
        val attachmentId = xmlService.storePaymentInstruction(paymentInstruction, ourIdentity)
        // TODO Check participant id

        val payments: List<Payment> = if (uniquePaymentIdToETEIdMap.isEmpty()) {
            // if empty, generate new UUIDs
            endToEndIds.map {
                Payment(
                    endToEndId = it,
                    uniquePaymentId = UUID.randomUUID(),
                    payer = ourIdentity,
                    gateway = gateway,
                    paymentInstructionId = attachmentId,
                    status = Payment.PaymentStatus.CREATED,
                )
            }
        } else if (uniquePaymentIdToETEIdMap.keys.containsAll(endToEndIds)) {
            // all keys are in the map, save each
            endToEndIds.map {
                Payment(
                    endToEndId = it,
                    uniquePaymentId = uniquePaymentIdToETEIdMap[it]!!,
                    payer = ourIdentity,
                    gateway = gateway,
                    paymentInstructionId = attachmentId,
                    status = Payment.PaymentStatus.CREATED,
                )
            }
        } else {
            throw Exception("Mismatched keys: 'unique payment' map does not contain all of the 'end to end' ids")
        }

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary)
            .addCommand(PaymentContract.Commands.Create(), ourIdentity.owningKey)
            .addAttachment(attachmentId)
        payments.forEach {
            builder.addOutputState(it)
        }
        builder.verify(serviceHub)

        val signedTransaction = serviceHub.signInitialTransaction(builder)

        subFlow(FinalityFlow(signedTransaction, listOf()))

        logger.info("startXxxFlow id=$uniquePaymentIdToETEIdMap")
        flowService.startXxxFlow(uniquePaymentIdToETEIdMap.values)

        return signedTransaction.tx.filterOutRefs { true }
    }
}
