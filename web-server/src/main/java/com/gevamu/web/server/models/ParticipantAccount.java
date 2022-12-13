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

package com.gevamu.web.server.models;

import com.gevamu.payments.app.contracts.schemas.AppSchemaV1;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ParticipantAccount {
    @NonNull
    String accountId;
    @NonNull
    String accountName;
    @NonNull
    String currency;

    public static ParticipantAccount fromDebtor(@NonNull AppSchemaV1.Debtor debtor) {
        return fromAccount(debtor.getAccount());
    }

    public static ParticipantAccount fromCreditor(@NonNull AppSchemaV1.Creditor creditor) {
        return fromAccount(creditor.getAccount());
    }

    private static ParticipantAccount fromAccount(@NonNull AppSchemaV1.Account account) {
        return ParticipantAccount.builder()
            .accountId(account.getAccount())
            .accountName(account.getAccountName())
            .currency(account.getCurrency().getIsoCode())
            .build();
    }
}
