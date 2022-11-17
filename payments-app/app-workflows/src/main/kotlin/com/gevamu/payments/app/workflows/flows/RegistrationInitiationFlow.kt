package com.gevamu.payments.app.workflows.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.flows.ParticipantRegistration
import com.gevamu.flows.RegisterParticipantFlow
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Registration
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import org.slf4j.LoggerFactory

@StartableByRPC
class RegistrationInitiationFlow(
    private val gateway: Party
) : FlowLogic<ParticipantRegistration>() {
    @Suspendable
    override fun call(): ParticipantRegistration {
        val registration = subFlow(RegisterParticipantFlow(gateway))

        serviceHub.withEntityManager {
            val entity = Registration(
                participantId = registration.participantId,
                networkId = registration.networkId
            )
            persist(entity)
        }

        return registration
    }
    companion object {
        private val log = LoggerFactory.getLogger(RegistrationInitiationFlow::class.java)
    }
}
