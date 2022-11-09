package com.gevamu.web.server.models;

import lombok.Value;

import java.util.List;

@Value
public class PaymentStateResponse {
    List<PaymentState> states;
}
