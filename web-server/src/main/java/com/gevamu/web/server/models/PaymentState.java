package com.gevamu.web.server.models;

import com.gevamu.states.Payment;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PaymentState {

    @NonNull
    String paymentId;

    @NonNull
    BigDecimal amount;

    @NonNull
    String currency;

    @NonNull
    Payment.PaymentStatus status;

    @NonNull
    ParticipantAccount beneficiary;
}
