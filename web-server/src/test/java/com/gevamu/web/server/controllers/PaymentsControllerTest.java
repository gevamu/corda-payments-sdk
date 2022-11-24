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

package com.gevamu.web.server.controllers;

import com.gevamu.flows.ParticipantRegistration;
import com.gevamu.payments.app.workflows.flows.PaymentInitiationRequest;
import com.gevamu.web.server.models.PaymentRequest;
import com.gevamu.web.server.services.CordaRpcClientService;
import com.gevamu.web.server.util.CompletableFutures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PaymentsControllerTest {

    private static final String PATH = "/api/v1/payments";

    @Autowired
    private transient WebTestClient webClient;

    @MockBean
    private transient CordaRpcClientService cordaRpcClientService;

    @BeforeEach
    public void beforeEach() {
        ParticipantRegistration registration = new ParticipantRegistration("test_p_id", "test_n_id");
        when(cordaRpcClientService.getRegistration())
            .thenReturn(CompletableFutures.completedStage(registration));
    }

    @AfterEach
    public void afterEach() {
        clearInvocations(cordaRpcClientService);
    }

    @Test
    public void testPostPayment() {

        PaymentRequest paymentRequest = new PaymentRequest("test_creditor_account", "test_debtor_account", BigDecimal.TEN);
        PaymentInitiationRequest paymentInitiationRequest = new PaymentInitiationRequest("test_creditor_account", "test_debtor_account", BigDecimal.TEN);
        when(cordaRpcClientService.sendPayment(paymentInitiationRequest))
            .thenReturn(CompletableFutures.completedStage(null));

        webClient.post()
            .uri(PATH)
            .bodyValue(paymentRequest)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .isEmpty();

        verify(cordaRpcClientService, times(1)).getRegistration();
        verify(cordaRpcClientService, times(1)).sendPayment(paymentInitiationRequest);
    }

    @Test
    public void testGetPaymentStates() {
        when(cordaRpcClientService.getPayments())
            .thenReturn(Collections.emptyList());

        webClient.get()
            .uri(PATH + "/states")
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("states")
            .isArray()
            .jsonPath("states.length()")
            .isEqualTo(0);
    }
}
