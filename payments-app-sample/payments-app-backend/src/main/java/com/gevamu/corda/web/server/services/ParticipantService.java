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

import com.gevamu.corda.web.server.config.Participant;
import com.gevamu.corda.web.server.config.Participants;
import com.gevamu.corda.web.server.util.MoreCollectors;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

@Service
public class ParticipantService {
    private final transient Map<String, Participant> creditors;

    private final transient Map<String, Participant> debtors;

    public ParticipantService(@NonNull Participants participants) {
        creditors = participants.getCreditors()
            .stream()
            .collect(MoreCollectors.toUnmodifiableMap(Participant::getAccount, Function.identity()));
        debtors = participants.getDebtors()
            .stream()
            .collect(MoreCollectors.toUnmodifiableMap(Participant::getAccount, Function.identity()));
    }

    public @NotNull Collection<Participant> getCreditors() {
        return creditors.values();
    }

    public @NotNull Collection<Participant> getDebtors() {
        return debtors.values();
    }

    public @NotNull Participant getCreditor(@NonNull String account) {
        return getParticipant("creditor", creditors, account);
    }

    public @NotNull Participant getDebtor(@NonNull String account) {
        return getParticipant("debtor", debtors, account);
    }

    private static @NotNull Participant getParticipant(
        @NotNull String participantType, @NotNull Map<String, Participant> participants, @NotNull String account
    ) {
        Participant participant = participants.get(account);
        if (participant == null) {
            throw new NoSuchElementException(String.format("There is no %s with account %s", participantType, account));
        }
        return participant;
    }
}
