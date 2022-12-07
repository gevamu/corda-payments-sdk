package com.gevamu.web.server.controllers;

import com.gevamu.flows.ParticipantRegistration;
import com.gevamu.payments.app.workflows.flows.PaymentInitiationRequest;
import com.gevamu.web.server.models.PaymentRequest;
import com.gevamu.web.server.services.CordaRpcClientService;
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
import java.util.concurrent.CompletableFuture;

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
            .thenReturn(CompletableFuture.completedFuture(registration));
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
            .thenReturn(CompletableFuture.completedFuture(null));

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
        when(cordaRpcClientService.getPaymentDetails())
            .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

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
