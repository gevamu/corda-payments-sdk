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
import java.util.stream.Collectors;

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
        return paymentService.processPayment(paymentRequest);
    }

    @GetMapping(
        path = "/states",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<PaymentStateResponse> getStates() {
        log.debug("getStates");
        return paymentService.getPaymentStates()
            .collect(Collectors.toList())
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
