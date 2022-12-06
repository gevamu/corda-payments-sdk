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

import com.gevamu.payments.app.workflows.flows.PaymentInitiationRequest;
import com.gevamu.web.server.models.PaymentRequest;
import com.gevamu.web.server.models.PaymentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PaymentService {

    @Autowired
    private transient RegistrationService registrationService;

    @Autowired
    private transient CordaRpcClientService cordaRpcClientService;

    public Mono<Void> processPayment(PaymentRequest paymentRequest) {
        return registrationService.getRegistration()
            .switchIfEmpty(Mono.error(ParticipantNotRegisteredException::new))
            .flatMap(it -> doProcessPayment(paymentRequest));
    }

    private Mono<Void> doProcessPayment(PaymentRequest paymentRequest) {
        return Mono.just(paymentRequest)
            .map(it -> new PaymentInitiationRequest(
                paymentRequest.getCreditorAccount(),
                paymentRequest.getDebtorAccount(),
                paymentRequest.getAmount()
            ))
            .flatMap(it -> Mono.defer(() -> Mono.fromCompletionStage(cordaRpcClientService.sendPayment(it))));
    }

    public Flux<PaymentState> getPaymentStates() {
        return Flux.fromIterable(cordaRpcClientService.getPaymentDetails());
    }
}
