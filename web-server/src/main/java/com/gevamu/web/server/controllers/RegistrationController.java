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
        return registrationService.getRegistration();
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ParticipantRegistration> postRegistration() {
        log.debug("postRegistration");
        return registrationService.register();
    }
}
