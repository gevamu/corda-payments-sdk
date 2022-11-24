package com.gevamu.payments.app.workflows.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.flows.ParticipantRegistration
import com.gevamu.payments.app.workflows.services.RegistrationService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party

@StartableByRPC
class RegistrationRetrievalFlow(
    private val gateway: Party
) : FlowLogic<ParticipantRegistration?>() {
    @Suspendable
    override fun call(): ParticipantRegistration? {
        val registrationService: RegistrationService = serviceHub.cordaService(RegistrationService::class.java)
        return registrationService.getRegistration()
    }
}
