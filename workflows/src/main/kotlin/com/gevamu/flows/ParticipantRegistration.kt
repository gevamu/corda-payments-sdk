package com.gevamu.flows

import java.io.Serializable
import net.corda.core.serialization.CordaSerializable

/**
 * Data class for Participant Corda node registration record
 *
 * @param participantId
 * @param networkId
 */
@CordaSerializable
data class ParticipantRegistration(
    /**
     * Unique id for Participant node in scope of Corda business network
     */
    val participantId: String,
    /**
     * Corda business network id
     */
    val networkId: String): Serializable {
    override fun equals(other: Any?): Boolean =
        (other is ParticipantRegistration) && this.participantId == other.participantId

    override fun hashCode(): Int {
        return this.participantId.hashCode()
    }

    companion object {
        private const val serialVersionUID: Long = 1L;
    }
}
