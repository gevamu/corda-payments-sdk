package com.gevamu.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class PaymentContract : Contract {
    companion object {
        val ID = "com.gevamu.contracts.PaymentContract"
    }

    override fun verify(tx: LedgerTransaction) {
    }

    interface Commands : CommandData {
        class Create : Commands
        class SendToGateway : Commands
        class UpdateStatus : Commands
    }
}
