package com.gevamu.flows

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class ParticipantRegistration(val participantId: String, val networkId: String) {
    override fun equals(other: Any?): Boolean =
        (other is ParticipantRegistration) && this.participantId == other.participantId

    override fun hashCode(): Int {
        return this.participantId.hashCode()
    }
}
