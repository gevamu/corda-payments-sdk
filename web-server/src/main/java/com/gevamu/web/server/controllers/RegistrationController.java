package com.gevamu.web.server.controllers;

import com.gevamu.flows.ParticipantRegistration;
import com.gevamu.web.server.services.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/registration")
@Slf4j
public class RegistrationController {

    @Autowired
    private transient RegistrationService registrationService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ParticipantRegistration> getRegistration() {
        log.debug("getRegistration");
        return Mono.defer(() -> {
            var registration = registrationService.getRegistration();
            return registration.map(Mono::just).orElseGet(Mono::empty);
        });
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ParticipantRegistration> postRegistration() {
        log.debug("postRegistration");
        return Mono.defer(() -> {
            var registration = registrationService.register();
            return Mono.fromCompletionStage(registration);
        });
    }
}
