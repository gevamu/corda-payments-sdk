package com.gevamu.web.server.services;

import com.gevamu.flows.ParticipantRegistration;
import net.openhft.chronicle.map.ChronicleMap;

import java.io.IOException;
import java.util.Optional;

public abstract class AbstractRegistrationStoreService implements RegistrationStoreService {

    private transient ChronicleMap<CharSequence, ParticipantRegistration> map;

    @Override
    public Optional<ParticipantRegistration> getRegistration() {
        return map.values().stream().findFirst();
    }

    @Override
    public void putRegistration(ParticipantRegistration registration) {
        map.put(registration.getParticipantId(), registration);
    }

    @Override
    public void close() {
        map.close();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        map = createMap();
    }

    protected abstract ChronicleMap<CharSequence, ParticipantRegistration> createMap() throws IOException;
}
