package com.gevamu.payments.app.workflows.test.services

import com.gevamu.payments.app.workflows.services.IdGeneratorService
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp.Companion.findCordapp
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IdGeneratorServiceTest {

    private lateinit var network: MockNetwork
    private lateinit var node: StartedMockNode
    private lateinit var idGeneratorService: IdGeneratorService

    @BeforeEach
    fun beforeEach() {
        network = MockNetwork(
            MockNetworkParameters(
                listOf(
                    findCordapp("com.gevamu.payments.app.workflows")
                )
            )
        )
        node = network.createNode()
        idGeneratorService = node.services.cordaService(IdGeneratorService::class.java)
    }

    @AfterEach
    fun afterEach() {
        network.stopNodes()
    }

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
