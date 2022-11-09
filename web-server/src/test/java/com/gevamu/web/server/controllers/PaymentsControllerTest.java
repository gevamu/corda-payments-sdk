package com.gevamu.web.server.controllers;

import com.gevamu.flows.ParticipantRegistration;
import com.gevamu.web.server.models.PaymentRequest;
import com.gevamu.web.server.services.CordaRpcClientService;
import com.gevamu.web.server.services.RegistrationService;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "participants.creditors[0].mic=test_creditor_mic",
        "participants.creditors[0].country=test_creditor_country",
        "participants.creditors[0].currency=test_creditor_currency",
        "participants.creditors[0].participantId=test_creditor_participantId",
        "participants.creditors[0].account=test_creditor_account",
        "participants.creditors[0].accountName=test_creditor_accountName",
        "participants.creditors[0].effectiveDate=test_creditor_effectiveDate",
        "participants.creditors[0].expiryDate=test_creditor_expiryDate",
        "participants.creditors[0].paymentLimit=test_creditor_paymentLimit",
        "participants.debtors[0].mic=test_debtor_mic",
        "participants.debtors[0].country=test_debtor_country",
        "participants.debtors[0].currency=test_debtor_currency",
        "participants.debtors[0].participantId=test_debtor_participantId",
        "participants.debtors[0].account=test_debtor_account",
        "participants.debtors[0].accountName=test_debtor_accountName",
        "participants.debtors[0].effectiveDate=test_debtor_effectiveDate",
        "participants.debtors[0].expiryDate=test_debtor_expiryDate",
        "participants.debtors[0].paymentLimit=test_debtor_paymentLimit"
    }
)
@ActiveProfiles("test")
public class PaymentsControllerTest {

    private static final String PATH = "/api/v1/payments";

    @Autowired
    private transient WebTestClient webClient;

    @MockBean
    private transient CordaRpcClientService cordaRpcClientService;

    @MockBean
    private transient RegistrationService registrationService;

    @BeforeEach
    public void beforeEach() {
        var registration = new ParticipantRegistration("test_p_id", "test_n_id");
        when(registrationService.getRegistration())
            .thenReturn(Optional.of(registration));
    }

    @AfterEach
    public void afterEach() {
        clearInvocations(cordaRpcClientService);
        clearInvocations(registrationService);
    }

    @Test
    public void testPostPayment() {
        when(cordaRpcClientService.executePaymentFlow(any()))
            .thenReturn(CompletableFuture.completedStage(null));

        var request = new PaymentRequest("test_creditor_account", BigDecimal.TEN);

        webClient.post()
            .uri(PATH)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .isEmpty();

        verify(cordaRpcClientService, times(1)).executePaymentFlow(any());
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
