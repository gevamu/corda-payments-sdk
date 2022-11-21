package com.gevamu.payments.app.workflows.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.flows.ParticipantRegistration
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Registration
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party

@StartableByRPC
class RegistrationRetrievalFlow(
    private val gateway: Party
) : FlowLogic<ParticipantRegistration?>() {
    @Suspendable
    override fun call(): ParticipantRegistration? = serviceHub.withEntityManager {
        val resultList = createQuery("FROM Registration", Registration::class.java)
            .resultList

        if (resultList.any()) {
            val registration = resultList.first()
            ParticipantRegistration(
                participantId = registration.participantId!!,
                networkId = registration.networkId!!
            )
        }

        null
    }
}
