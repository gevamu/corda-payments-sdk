package com.gevamu.web.server.config;

import lombok.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Scope;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@ConfigurationProperties("corda.rpc.client.connection")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ConstructorBinding
@Validated
@Value
public class CordaRpcClientConnection {

    @NotBlank
    String host;

    @Min(1025)
    @Max(65535)
    int port;

    @NotBlank
    String user;

    @NotBlank
    String password;
}
