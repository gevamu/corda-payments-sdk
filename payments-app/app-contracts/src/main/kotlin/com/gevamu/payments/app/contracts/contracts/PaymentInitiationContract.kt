package com.gevamu.payments.app.contracts.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class PaymentInitiationContract : Contract {
    override fun verify(tx: LedgerTransaction) {
    }
    interface Commands : CommandData {
        class Create : Commands
    }
}
