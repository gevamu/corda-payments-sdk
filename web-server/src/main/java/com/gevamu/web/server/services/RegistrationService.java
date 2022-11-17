package com.gevamu.web.server.services;

import com.gevamu.flows.ParticipantRegistration;
import com.gevamu.payments.app.workflows.flows.RegistrationInitiationFlow;
import com.gevamu.payments.app.workflows.flows.RegistrationRetrievalFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RegistrationService {

    @Autowired
    private transient CordaRpcClientService cordaRpcClientService;

    public Mono<ParticipantRegistration> getRegistration() {
        return Mono.defer(
            () -> Mono.fromCompletionStage(
                cordaRpcClientService.executeFlow(RegistrationRetrievalFlow.class)
            )
        );
    }

    public Mono<ParticipantRegistration> register() {
        return getRegistration().switchIfEmpty(doRegister());
    }

    private Mono<ParticipantRegistration> doRegister() {
        return Mono.defer(
            () -> Mono.fromCompletionStage(
                cordaRpcClientService.executeFlow(RegistrationInitiationFlow.class)
            )
        );
    }
}
