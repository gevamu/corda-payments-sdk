package com.gevamu.web.server.services;

import com.gevamu.flows.ParticipantRegistration;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Profile("!test")
public class FileRegistrationStoreService extends AbstractRegistrationStoreService {
    @Override
    protected ChronicleMap<CharSequence, ParticipantRegistration> createMap() throws IOException {
        Path directory = Paths.get(System.getProperty("user.dir"), "data");
        Path file = directory.resolve("registration.dat");
        Files.createDirectories(directory);
        return ChronicleMapBuilder.of(CharSequence.class, ParticipantRegistration.class)
            .name("file-registration-store")
            .entries(1)
            .averageKeySize(36)
            .averageValueSize(208)
            .createPersistedTo(file.toFile());
    }
}
