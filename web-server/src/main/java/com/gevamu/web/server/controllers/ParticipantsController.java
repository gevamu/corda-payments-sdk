package com.gevamu.web.server.controllers;

import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1;
import com.gevamu.web.server.models.ParticipantAccount;
import com.gevamu.web.server.models.ParticipantAccountResponse;
import com.gevamu.web.server.services.ParticipantService;
import com.gevamu.web.server.util.MoreCollectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

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
        return Mono.defer(() -> Mono.fromCompletionStage(participantService.getCreditors()))
            .map(this::toResponse);
    }

    @GetMapping(
        path = "/debtors",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ParticipantAccountResponse> getDebtors() {
        log.debug("getDebtors");
        return Mono.defer(() -> Mono.fromCompletionStage(participantService.getDebtors()))
            .map(this::toResponse);
    }

    private ParticipantAccountResponse toResponse(List<? extends AccountSchemaV1.Account> accounts) {
        List<ParticipantAccount> participantAccounts = accounts.stream()
            .map(it -> ParticipantAccount.builder()
                .accountId(it.getAccount())
                .accountName(it.getAccountName())
                .currency(it.getCurrency().getIsoCode())
                .build()
            )
            .collect(MoreCollectors.toUnmodifiableList());
        return new ParticipantAccountResponse(participantAccounts);
    }
}
