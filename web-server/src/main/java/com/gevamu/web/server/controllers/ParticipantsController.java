package com.gevamu.web.server.controllers;

import com.gevamu.web.server.models.ParticipantAccount;
import com.gevamu.web.server.models.ParticipantAccountResponse;
import com.gevamu.web.server.services.ParticipantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/participants")
@Slf4j
public class ParticipantsController {

    @Autowired
    private transient ParticipantService participantService;

    @GetMapping(
        path = "/creditors",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ParticipantAccountResponse> getCreditors() {
        log.debug("getCreditors");
        return Mono.defer(
            () -> Mono.just(participantService.getCreditors())
                .map(it -> it.stream()
                    .map(p -> ParticipantAccount.builder()
                        .accountId(p.getAccount())
                        .accountName(p.getAccountName())
                        .currency(p.getCurrency())
                        .build()
                    )
                    .sorted(Comparator.comparing(ParticipantAccount::getAccountName))
                    .collect(Collectors.toUnmodifiableList())
                )
                .map(ParticipantAccountResponse::new)
        );
    }
}
