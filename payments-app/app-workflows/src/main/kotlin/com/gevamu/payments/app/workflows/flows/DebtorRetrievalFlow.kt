package com.gevamu.payments.app.workflows.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Account
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Debtor
import java.util.stream.Collectors
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party

@StartableByRPC
class DebtorRetrievalFlow(
    private val gateway: Party
) : FlowLogic<List<Account>>() {
    @Suspendable
    override fun call(): List<Account> = serviceHub.withEntityManager {
        createQuery("FROM Debtor", Debtor::class.java).resultList
            .stream()
            .map(Debtor::account)
            .collect(Collectors.toList())
    } as List<Account>
}
