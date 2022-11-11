package com.gevamu.web.server.services;

import com.gevamu.flows.ParticipantRegistration;
import org.springframework.beans.factory.InitializingBean;

import java.util.Optional;

public interface RegistrationStoreService extends InitializingBean, AutoCloseable {
    Optional<ParticipantRegistration> getRegistration();
    void putRegistration(ParticipantRegistration registration);
}
