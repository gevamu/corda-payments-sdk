package com.gevamu.web.server.services;

import com.gevamu.flows.ParticipantRegistration;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class InMemoryRegistrationStoreService extends AbstractRegistrationStoreService {
    @Override
    protected ChronicleMap<CharSequence, ParticipantRegistration> createMap() {
        return ChronicleMapBuilder.of(CharSequence.class, ParticipantRegistration.class)
            .name("in-memory-registration-store")
            .entries(1)
            .averageKeySize(36)
            .averageValueSize(208)
            .create();
    }
}
