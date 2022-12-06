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
