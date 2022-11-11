package com.gevamu.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap

@InitiatingFlow
@StartableByRPC
class RegisterParticipantFlow(private val gateway: Party) : FlowLogic<ParticipantRegistration>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): ParticipantRegistration {
        val gatewaySession: FlowSession = initiateFlow(gateway)
        return gatewaySession.receive<ParticipantRegistration>().unwrap { it }
    }
}
