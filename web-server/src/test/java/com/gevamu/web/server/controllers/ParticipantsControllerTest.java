package com.gevamu.web.server.controllers;

import com.gevamu.web.server.services.CordaRpcClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "participants.creditors[0].mic=test_mic",
        "participants.creditors[0].country=test_country",
        "participants.creditors[0].currency=test_currency",
        "participants.creditors[0].account=test_account",
        "participants.creditors[0].accountName=test_accountName",
        "participants.creditors[0].effectiveDate=test_effectiveDate",
        "participants.creditors[0].expiryDate=test_expiryDate",
        "participants.creditors[0].paymentLimit=test_paymentLimit"
    }
)
@ActiveProfiles("test")
public class ParticipantsControllerTest {

    private static final String PATH = "/api/v1/participants";

    @Autowired
    private transient WebTestClient webClient;

    @MockBean
    private transient CordaRpcClientService cordaRpcClientService;

    @Test
    public void testCreditors() {
        webClient.get()
            .uri(PATH + "/creditors")
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("accounts")
            .isArray()
            .jsonPath("accounts.length()")
            .isEqualTo(1)
            .jsonPath("accounts[0].accountId")
            .isEqualTo("test_account")
            .jsonPath("accounts[0].accountName")
            .isEqualTo("test_accountName")
            .jsonPath("accounts[0].currency")
            .isEqualTo("test_currency");
    }
}
