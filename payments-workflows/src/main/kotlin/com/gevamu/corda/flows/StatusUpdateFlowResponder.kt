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
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.unwrap

@InitiatedBy(StatusUpdateFlow::class)
class StatusUpdateFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signedTransactionId = receiveAndSignTransaction()
        subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = signedTransactionId))
    }

    private val signTransactionFlow: SignTransactionFlow get() =
        object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) {
                // TODO Check it's expected transaction
            }
        }

    @Suspendable
    private fun nextStep(): StatusUpdateFlow.FlowStep =
        counterpartySession.receive(StatusUpdateFlow.FlowStep::class.java, maySkipCheckpoint = true).unwrap { it }

    @Suspendable
    private fun receiveAndSignTransaction(): SecureHash {
        var signedTransactionId: SecureHash
        do {
            signedTransactionId = subFlow(signTransactionFlow).id
        } while (nextStep() == StatusUpdateFlow.FlowStep.SIGN_TRANSACTION)
        return signedTransactionId
    }
}
