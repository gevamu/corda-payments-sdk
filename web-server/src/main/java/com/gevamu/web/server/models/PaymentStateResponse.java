package com.gevamu.web.server.models;

import com.gevamu.payments.app.workflows.services.PaymentState;
import lombok.Value;

import java.util.List;

@Value
public class PaymentStateResponse {
    List<? extends PaymentState> states;
}
