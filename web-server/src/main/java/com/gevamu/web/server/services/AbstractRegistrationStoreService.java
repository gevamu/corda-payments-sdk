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
