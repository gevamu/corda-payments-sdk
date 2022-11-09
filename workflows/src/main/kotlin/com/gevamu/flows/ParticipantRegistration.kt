package com.gevamu.flows

import java.io.Serializable
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class ParticipantRegistration(val participantId: String, val networkId: String): Serializable {
    override fun equals(other: Any?): Boolean =
        (other is ParticipantRegistration) && this.participantId == other.participantId

    override fun hashCode(): Int {
        return this.participantId.hashCode()
    }

    companion object {
        private const val serialVersionUID: Long = 1L;
    }
}
