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
        val entity = Registration()
        entity.participantId = registration.participantId
        entity.networkId = registration.networkId
        persist(entity)
    }
}
