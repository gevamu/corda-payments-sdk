package com.gevamu.web.server.controllers;

import com.gevamu.flows.ParticipantRegistration;
import com.gevamu.web.server.models.PaymentRequest;
import com.gevamu.web.server.services.CordaRpcClientService;
import com.gevamu.web.server.util.CompletableFutures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PaymentsControllerNegativeTest {

    private static final String PATH = "/api/v1/payments";

    private static final String TEST_CREDITOR_ACCOUNT = "test_creditor_account";
    private static final String TEST_DEBTOR_ACCOUNT = "test_debtor_account";

    @Autowired
    private transient WebTestClient webClient;

    @MockBean
    private transient CordaRpcClientService cordaRpcClientService;

    @BeforeEach
    public void beforeEach() {
        ParticipantRegistration registration = new ParticipantRegistration("test_p_id", "test_n_id");
        when(cordaRpcClientService.getRegistration())
            .thenReturn(CompletableFutures.completedStage(registration));
        when(cordaRpcClientService.sendPayment(any()))
            .thenReturn(CompletableFutures.completedStage(null));
    }

    @AfterEach
    public void afterEach() {
        verify(cordaRpcClientService, never()).executePaymentFlow(any());
        clearInvocations(cordaRpcClientService);
    }

    @Test
    public void testPostPaymentNullCreditorAccount() {
        PaymentRequest request = new PaymentRequest(null, TEST_DEBTOR_ACCOUNT, BigDecimal.TEN);
        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testPostPaymentEmptyCreditorAccount() {
        PaymentRequest request = new PaymentRequest("", TEST_DEBTOR_ACCOUNT, BigDecimal.TEN);
        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testPostPaymentBlankCreditorAccount() {
        PaymentRequest request = new PaymentRequest(" ", TEST_DEBTOR_ACCOUNT, BigDecimal.TEN);
        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testPostPaymentNullDebtorAccount() {
        PaymentRequest request = new PaymentRequest(TEST_CREDITOR_ACCOUNT, null, BigDecimal.TEN);
        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testPostPaymentEmptyDebtorAccount() {
        PaymentRequest request = new PaymentRequest(TEST_CREDITOR_ACCOUNT, "", BigDecimal.TEN);
        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testPostPaymentBlankDebtorAccount() {
        PaymentRequest request = new PaymentRequest(TEST_CREDITOR_ACCOUNT, " ", BigDecimal.TEN);
        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testPostPaymentNullAmount() {
        PaymentRequest request = new PaymentRequest(TEST_CREDITOR_ACCOUNT, TEST_DEBTOR_ACCOUNT, null);
        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testPostPaymentZeroAmount() {
        PaymentRequest request = new PaymentRequest(TEST_CREDITOR_ACCOUNT, TEST_DEBTOR_ACCOUNT, BigDecimal.valueOf(0.0));
        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testPostPaymentNegativeAmount() {
        PaymentRequest request = new PaymentRequest(TEST_CREDITOR_ACCOUNT, TEST_DEBTOR_ACCOUNT, BigDecimal.valueOf(-100.0));
        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testPostPaymentParticipantNotRegistered() {
        when(cordaRpcClientService.getRegistration())
            .thenReturn(CompletableFutures.completedStage(null));
        PaymentRequest request = new PaymentRequest(TEST_CREDITOR_ACCOUNT, TEST_DEBTOR_ACCOUNT, BigDecimal.TEN);
        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isForbidden();
    }
}
