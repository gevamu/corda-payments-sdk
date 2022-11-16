/*
 * Copyright 2022 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
