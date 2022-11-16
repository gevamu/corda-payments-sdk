package com.gevamu.web.server.services;

import com.gevamu.iso20022.pain.CreditTransferTransaction34;
import com.gevamu.iso20022.pain.PaymentInstruction30;
import com.gevamu.payments.app.contracts.states.PaymentDetails;
import com.gevamu.payments.app.workflows.flows.PaymentInitiationFlow;
import com.gevamu.payments.app.workflows.flows.PaymentInitiationRequest;
import com.gevamu.states.Payment;
import com.gevamu.web.server.models.ParticipantAccount;
import com.gevamu.web.server.models.PaymentRequest;
import com.gevamu.web.server.models.PaymentState;
import com.gevamu.web.server.util.CompletableFutures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private transient RegistrationService registrationService;

    @Autowired
    private transient CordaRpcClientService cordaRpcClientService;

    public CompletionStage<Void> processPayment(PaymentRequest paymentRequest) {
        PaymentInitiationRequest request = new PaymentInitiationRequest(
            paymentRequest.getCreditorAccount(),
            paymentRequest.getDebtorAccount(),
            paymentRequest.getAmount()
        );
        try {
            registrationService.getRegistration()
                .orElseThrow(ParticipantNotRegisteredException::new);
            return cordaRpcClientService.executeFlow(PaymentInitiationFlow.class, request)
                .thenApply(it -> null);
        }
        catch (Exception e) {
            return CompletableFutures.failedStage(e);
        }
    }

    public Mono<List<PaymentState>> getPaymentStates() {
        return Flux.zip(
            Flux.fromIterable(cordaRpcClientService.getPaymentDetails()),
            Flux.fromIterable(cordaRpcClientService.getPayments())
        ).map(it -> {
            PaymentDetails details = it.getT1();
            Payment payment = it.getT2();
            ParticipantAccount debtor = ParticipantAccount.builder()
                .accountId(details.getDebtor().getAccountId())
                .accountName(details.getDebtor().getAccountName())
                .currency(details.getDebtor().getCurrency())
                .build();
            ParticipantAccount creditor = ParticipantAccount.builder()
                .accountId(details.getCreditor().getAccountId())
                .accountName(details.getCreditor().getAccountName())
                .currency(details.getCreditor().getCurrency())
                .build();
            return PaymentState.builder()
                .paymentId(payment.getLinearId().toString())
                .status(payment.getStatus())
                .updateTime(payment.getTimestamp())
                .creationTime(details.getCreationTime())
                .endToEndId(details.getEndToEndId())
                .currency(details.getCurrency())
                .debtor(debtor)
                .creditor(creditor)
                .amount(details.getAmount())
                .build();

        }).collect(Collectors.toList());
    }

    private ParticipantAccount deriveCreditor(CreditTransferTransaction34 transaction) {
        String creditor = transaction.getCdtr().getNm();
        String creditorAccount = transaction.getCdtrAcct().getId().getOthr().getId();
        String creditorCurrency = transaction.getCdtrAcct().getCcy();
        return ParticipantAccount.builder()
            .accountId(creditorAccount)
            .accountName(creditor)
            .currency(creditorCurrency)
            .build();
    }

    private ParticipantAccount deriveDebtor(PaymentInstruction30 paymentInstruction) {
        String creditor = paymentInstruction.getDbtr().getNm();
        String creditorAccount = paymentInstruction.getDbtrAcct().getId().getOthr().getId();
        String creditorCurrency = paymentInstruction.getDbtrAcct().getCcy();
        return ParticipantAccount.builder()
            .accountId(creditorAccount)
            .accountName(creditor)
            .currency(creditorCurrency)
            .build();
    }
}
