/*
 * Copyright 2022 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gevamu.corda.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap
import java.io.Serializable

/**
 * Data class for Participant Corda node registration record
 *
 * @param participantId Unique id for Participant node in scope of Corda business network
 * @param networkId Corda business network id
 */
@CordaSerializable
data class ParticipantRegistration(val participantId: String, val networkId: String) : Serializable {
    override fun equals(other: Any?): Boolean =
        (other is ParticipantRegistration) && this.participantId == other.participantId

    override fun hashCode(): Int {
        return this.participantId.hashCode()
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

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
