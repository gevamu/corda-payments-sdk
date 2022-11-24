package com.gevamu.web.server.models;

import lombok.EqualsAndHashCode;
import lombok.Value;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Value
@EqualsAndHashCode
public class PaymentRequest {
    @NotBlank
    String creditorAccount;
    @NotBlank
    String debtorAccount;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal amount;
}
