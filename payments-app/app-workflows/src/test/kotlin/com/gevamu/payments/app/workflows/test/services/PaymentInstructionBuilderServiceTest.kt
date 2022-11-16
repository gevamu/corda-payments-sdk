package com.gevamu.payments.app.workflows.test.services

import com.gevamu.payments.app.workflows.services.PaymentInstructionBuilderService
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PaymentInstructionBuilderServiceTest {

    private lateinit var network: MockNetwork
    private lateinit var node: StartedMockNode
    private lateinit var paymentInstructionBuilderService: PaymentInstructionBuilderService

    @BeforeEach
    fun beforeEach() {
        network = MockNetwork(
            MockNetworkParameters(
                listOf(
                    TestCordapp.findCordapp("com.gevamu.payments.app.workflows")
                )
            )
        )
        node = network.createNode()
        paymentInstructionBuilderService = node.services.cordaService(PaymentInstructionBuilderService::class.java)
    }

    @AfterEach
    fun afterEach() {
        network.stopNodes()
    }

    @Test
    fun test() {

    }
}
