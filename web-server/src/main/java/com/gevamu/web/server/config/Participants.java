package com.gevamu.web.server.config;

import lombok.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Scope;

import java.util.List;

@ConfigurationProperties("participants")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ConstructorBinding
@Value
public class Participants {
    List<Participant> creditors;
    List<Participant> debtors;
}
