// Copyright 2022 Exactpro Systems Limited
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.gevamu.web.server.controllers;

import com.gevamu.web.server.config.Participant;
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

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Supplier;
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
        return getAccounts(() -> participantService.getCreditors());
    }

    @GetMapping(
        path = "/debtors",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ParticipantAccountResponse> getDebtors() {
        log.debug("getDebtors");
        return getAccounts(() -> participantService.getDebtors());
    }

    private Mono<ParticipantAccountResponse> getAccounts(Supplier<Collection<Participant>> supplier) {
        return Mono.defer(
            () -> Mono.just(supplier.get())
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
