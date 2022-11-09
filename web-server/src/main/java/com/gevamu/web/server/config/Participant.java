package com.gevamu.web.server.config;

import lombok.Value;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@Value
public class Participant {
    String bic;
    String country;
    String currency;
    String account;
    String accountName;
    String effectiveDate;
    String expiryDate;
    String paymentLimit;
}
