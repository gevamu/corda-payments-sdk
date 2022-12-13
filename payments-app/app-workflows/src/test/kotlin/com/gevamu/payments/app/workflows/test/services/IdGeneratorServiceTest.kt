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

package com.gevamu.payments.app.workflows.test.services

import SpyServices
import com.gevamu.payments.app.workflows.services.IdGeneratorService
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Test

class IdGeneratorServiceTest {

    private val services: SpyServices = SpyServices()
    private val idGeneratorService: IdGeneratorService = services.cordaService(IdGeneratorService::class.java)

    @Test
    fun testGenerateId() {
        val id = idGeneratorService.generateId()
        assertId(id, 32)
    }

    @Test
    fun testGenerateEndToEndId() {
        val id = idGeneratorService.generateEndToEndId()
        assertId(id, 13)
    }

    private fun assertId(id: String?, expectedLength: Int) {
        AssertionsForClassTypes.assertThat(id).isNotBlank
        AssertionsForClassTypes.assertThat(id).isLowerCase
        AssertionsForClassTypes.assertThat(StringUtils.isAlphanumeric(id)).isTrue
        AssertionsForClassTypes.assertThat(id).hasSize(expectedLength)
    }
}
