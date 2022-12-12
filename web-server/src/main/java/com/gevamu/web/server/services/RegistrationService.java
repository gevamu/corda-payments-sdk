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

package com.gevamu.web.server.services;

import com.gevamu.flows.ParticipantRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RegistrationService {

    @Autowired
    private transient CordaRpcClientService cordaRpcClientService;

    public Mono<ParticipantRegistration> getRegistration() {
        return Mono.defer(() -> Mono.fromCompletionStage(cordaRpcClientService.getRegistration()));
    }

    public Mono<ParticipantRegistration> register() {
        return getRegistration().switchIfEmpty(doRegister());
    }

    private Mono<ParticipantRegistration> doRegister() {
        return Mono.defer(() -> Mono.fromCompletionStage(cordaRpcClientService.register()));
    }
}
