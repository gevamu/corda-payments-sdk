package com.gevamu.web.server.services;

import com.gevamu.flows.ParticipantRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Service
public class RegistrationService {

    @Autowired
    private transient CordaRpcClientService cordaRpcClientService;

    @Autowired
    private transient RegistrationStoreService store;

    public Optional<ParticipantRegistration> getRegistration() {
        return store.getRegistration();
    }

    public CompletionStage<ParticipantRegistration> register() {
        return doRegister().thenApply(it -> {
            store.putRegistration(it);
            return it;
        });
    }

    private CompletionStage<ParticipantRegistration> doRegister() {
        return cordaRpcClientService.executeRegistrationFlow();
    }
}
