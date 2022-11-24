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
