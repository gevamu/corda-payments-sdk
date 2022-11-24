package com.gevamu.web.server.controllers;

import com.gevamu.flows.ParticipantRegistration;
import com.gevamu.payments.app.workflows.flows.RegistrationInitiationFlow;
import com.gevamu.payments.app.workflows.flows.RegistrationRetrievalFlow;
import com.gevamu.web.server.services.CordaRpcClientService;
import com.gevamu.web.server.services.RegistrationService;
import com.gevamu.web.server.util.CompletableFutures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

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

    @AfterEach
    public void afterEach() {
        clearInvocations(cordaRpcClientService);
    }

    @Test
    public void testGetRegistrationWhenNotRegistered() {
        when(cordaRpcClientService.executeFlow(RegistrationRetrievalFlow.class))
            .thenReturn(CompletableFutures.completedStage(null));
        webClient.get()
            .uri(PATH)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .isEmpty();
    }

    @Test
    public void testGetRegistrationWhenRegistered() {
        ParticipantRegistration registration = new ParticipantRegistration("test_p_id", "test_n_id");
        when(cordaRpcClientService.executeFlow(RegistrationRetrievalFlow.class))
            .thenReturn(CompletableFutures.completedStage(registration));
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

    @Test
    public void testPostRegistration() {
        ParticipantRegistration registration = new ParticipantRegistration("test_p_id", "test_n_id");
        when(cordaRpcClientService.executeFlow(RegistrationInitiationFlow.class))
            .thenReturn(CompletableFutures.completedStage(registration));
        when(cordaRpcClientService.executeFlow(RegistrationRetrievalFlow.class))
            .thenReturn(CompletableFutures.completedStage(null));
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
    }
}
