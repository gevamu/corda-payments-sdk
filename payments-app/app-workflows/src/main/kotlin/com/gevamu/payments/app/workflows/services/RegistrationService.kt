package com.gevamu.payments.app.workflows.services

import com.gevamu.flows.ParticipantRegistration
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1.Registration
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class RegistrationService(
    private val serviceHub: AppServiceHub
) : SingletonSerializeAsToken() {

    fun getRegistration(): ParticipantRegistration? = serviceHub.withEntityManager {
        val registration = createQuery("FROM Registration", Registration::class.java)
            .resultList.firstOrNull()

        if (registration != null)
            ParticipantRegistration(
                participantId = registration.participantId!!,
                networkId = registration.networkId!!
            )
        else null
    }

    fun saveRegistration(registration: ParticipantRegistration) = serviceHub.withEntityManager {
        val entity = Registration(
            participantId = registration.participantId,
            networkId = registration.networkId
        )
        persist(entity)
    }
}
