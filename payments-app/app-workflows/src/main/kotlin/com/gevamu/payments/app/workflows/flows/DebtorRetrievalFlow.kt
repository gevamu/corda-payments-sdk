package com.gevamu.payments.app.workflows.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.Account
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.Debtor
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import java.util.stream.Collectors

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
