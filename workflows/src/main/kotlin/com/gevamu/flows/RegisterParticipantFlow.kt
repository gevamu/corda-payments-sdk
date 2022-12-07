package com.gevamu.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap

/**
 * Register your participant node in Gevamu Gateway node
 *
 * @param gateway Identification of the Gevamu Gateway Corda node to register
 * @see Party
 *
 * @return Participant registration record
 * @see ParticipantRegistration
 */
@InitiatingFlow
@StartableByRPC
class RegisterParticipantFlow(private val gateway: Party) : FlowLogic<ParticipantRegistration>() {
    override val progressTracker = ProgressTracker()

    /**
     * Start main flow logic
     *
     * @return Participant registration note
     * @see ParticipantRegistration
     */
    @Suspendable
    override fun call(): ParticipantRegistration {
        val gatewaySession: FlowSession = initiateFlow(gateway)
        return gatewaySession.receive<ParticipantRegistration>().unwrap { it }
    }
}
