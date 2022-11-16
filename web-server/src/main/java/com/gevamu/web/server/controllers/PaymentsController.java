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

import com.gevamu.web.server.models.PaymentRequest;
import com.gevamu.web.server.models.PaymentStateResponse;
import com.gevamu.web.server.services.ParticipantNotRegisteredException;
import com.gevamu.web.server.services.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/payments")
@Slf4j
public class PaymentsController {

    @Autowired
    private transient PaymentService paymentService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> postPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        log.debug("postPayment: {}", paymentRequest);
        return Mono.defer(() -> Mono.fromCompletionStage(paymentService.processPayment(paymentRequest)));
    }

    @GetMapping(
        path = "/states",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<PaymentStateResponse> getStates() {
        log.debug("getStates");
        return paymentService.getPaymentStates()
            .map(PaymentStateResponse::new);
    }

    @ResponseStatus(
        value = HttpStatus.FORBIDDEN,
        reason = "Participant not registered"
    )
    @ExceptionHandler(ParticipantNotRegisteredException.class)
    public void participantNotRegisteredHandler() {
    }
}
