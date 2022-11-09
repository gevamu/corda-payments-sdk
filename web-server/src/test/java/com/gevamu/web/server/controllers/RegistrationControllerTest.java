package com.gevamu.web.server.controllers;

import com.gevamu.flows.ParticipantRegistration;
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

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RegistrationControllerTest {

    private static final String PATH = "/api/v1/registration";

    private transient RegistrationService registrationService;



    @Autowired
    private transient WebTestClient webClient;

    @MockBean
    private transient CordaRpcClientService cordaRpcClientService;

    @BeforeEach
    public void beforeEach() {
        var registration = new ParticipantRegistration("test_p_id", "test_n_id");
        when(cordaRpcClientService.executeRegistrationFlow())
            .thenReturn(CompletableFuture.completedStage(registration));
    }

    @AfterEach
    public void afterEach() {
        clearInvocations(cordaRpcClientService);
    }

    @Test
    public void test() {
        webClient.get()
            .uri(PATH)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .isEmpty();

        webClient.post()
            .uri(PATH)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("participantId")
            .isEqualTo("test_p_id")
            .jsonPath("networkId")
            .isEqualTo("test_n_id");

        webClient.get()
            .uri(PATH)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("participantId")
            .isEqualTo("test_p_id")
            .jsonPath("networkId")
            .isEqualTo("test_n_id");
    }
}
