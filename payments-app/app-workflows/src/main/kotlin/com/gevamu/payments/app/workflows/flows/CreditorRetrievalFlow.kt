package com.gevamu.payments.app.workflows.flows

import co.paralleluniverse.fibers.Suspendable

import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Account
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Creditor
import java.util.stream.Collectors
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party

@StartableByRPC
class CreditorRetrievalFlow(
    private val gateway: Party
) : FlowLogic<List<Account>>() {
    @Suspendable
    override fun call(): List<Account> = serviceHub.withEntityManager {
        createQuery("FROM Creditor", Creditor::class.java).resultList
            .stream()
            .map(Creditor::account)
            .collect(Collectors.toList())
    } as List<Account>
}
