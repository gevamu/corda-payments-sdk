package com.gevamu.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.transactions.SignedTransaction

@InitiatedBy(StatusUpdateFlow::class)
class StatusUpdateFlowResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        // confirm transaction
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) {}
        }

        // sign transaction on payer side
        val signedTransaction = subFlow(signTransactionFlow)

        // finish transaction on our side
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = signedTransaction.id))
    }
}
