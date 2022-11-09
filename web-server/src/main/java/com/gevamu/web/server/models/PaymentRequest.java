package com.gevamu.web.server.models;

import lombok.Value;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Value
public class PaymentRequest {
    @NotBlank
    String beneficiaryAccount;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal amount;
}
