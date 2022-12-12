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

import com.gevamu.payments.app.workflows.services.EntityManagerService
import com.gevamu.payments.app.workflows.services.IdGeneratorService
import com.gevamu.payments.app.workflows.services.PaymentInstructionBuilderService
import com.gevamu.payments.app.workflows.services.RegistrationService
import net.corda.core.serialization.SerializeAsToken
import net.corda.testing.node.MockServices
import net.corda.testing.node.createMockCordaService
import org.mockito.Mockito.spy

class SpyServices(
    private val services: MockServices = MockServices(listOf("com.gevamu.payments.app.workflows"))
) {
    init {
        createMockCordaService(services) { spy(RegistrationService(it)) }
        createMockCordaService(services) { spy(PaymentInstructionBuilderService(it)) }
        createMockCordaService(services) { spy(IdGeneratorService(it)) }
        createMockCordaService(services) { spy(EntityManagerService(it)) }
    }

    fun <T : SerializeAsToken> cordaService(type: Class<T>): T = services.cordaService(type)
}
