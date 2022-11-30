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
