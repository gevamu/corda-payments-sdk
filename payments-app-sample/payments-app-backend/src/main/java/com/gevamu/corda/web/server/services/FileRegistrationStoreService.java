/*
 * Copyright 2022 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gevamu.corda.web.server.services;

import com.gevamu.corda.flows.ParticipantRegistration;
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
