package com.gevamu.web.server.models;

import com.gevamu.states.Payment;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class PaymentState {
    @NonNull
    Instant creationTime;
    @NonNull
    Instant updateTime;
    @NonNull
    String paymentId;
    @NonNull
    String endToEndId;
    @NonNull
    BigDecimal amount;
    @NonNull
    String currency;
    @NonNull
    Payment.PaymentStatus status;
    @NonNull
    ParticipantAccount creditor;
    @NonNull
    ParticipantAccount debtor;
}
