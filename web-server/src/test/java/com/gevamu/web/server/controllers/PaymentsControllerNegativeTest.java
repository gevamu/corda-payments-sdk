package com.gevamu.web.server.controllers;

import com.gevamu.flows.ParticipantRegistration;
import com.gevamu.web.server.models.PaymentRequest;
import com.gevamu.web.server.services.CordaRpcClientService;
import com.gevamu.web.server.services.RegistrationService;
import com.gevamu.web.server.util.CompletableFutures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "participants.creditors[0].bic=test_creditor_bic",
        "participants.creditors[0].country=test_creditor_country",
        "participants.creditors[0].currency=test_creditor_currency",
        "participants.creditors[0].account=test_creditor_account",
        "participants.creditors[0].accountName=test_creditor_accountName",
        "participants.creditors[0].effectiveDate=test_creditor_effectiveDate",
        "participants.creditors[0].expiryDate=test_creditor_expiryDate",
        "participants.creditors[0].paymentLimit=test_creditor_paymentLimit",
        "participants.debtors[0].bic=test_debtor_bic",
        "participants.debtors[0].country=test_debtor_country",
        "participants.debtors[0].currency=test_debtor_currency",
        "participants.debtors[0].account=test_debtor_account",
        "participants.debtors[0].accountName=test_debtor_accountName",
        "participants.debtors[0].effectiveDate=test_debtor_effectiveDate",
        "participants.debtors[0].expiryDate=test_debtor_expiryDate",
        "participants.debtors[0].paymentLimit=test_debtor_paymentLimit"
    }
)
@ActiveProfiles("test")
public class PaymentsControllerNegativeTest {

    private static final String PATH = "/api/v1/payments";

    private static final String TEST_CREDITOR_ACCOUNT = "test_creditor_account";
    private static final String TEST_DEBTOR_ACCOUNT = "test_debtor_account";

    @Autowired
    private transient WebTestClient webClient;

    @MockBean
    private transient CordaRpcClientService cordaRpcClientService;

    @MockBean
    private transient RegistrationService registrationService;

    @BeforeEach
    public void beforeEach() {
        ParticipantRegistration registration = new ParticipantRegistration("test_p_id", "test_n_id");
        when(registrationService.getRegistration())
            .thenReturn(Mono.just(registration));
        when(cordaRpcClientService.executePaymentFlow(any()))
            .thenReturn(CompletableFutures.completedStage(null));
    }

    @AfterEach
    public void afterEach() {
        verify(cordaRpcClientService, never()).executePaymentFlow(any());
        clearInvocations(cordaRpcClientService);
        clearInvocations(registrationService);
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
        when(registrationService.getRegistration())
            .thenReturn(Mono.empty());
        PaymentRequest request = new PaymentRequest(TEST_CREDITOR_ACCOUNT, TEST_DEBTOR_ACCOUNT, BigDecimal.TEN);
        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isForbidden();
    }
}
