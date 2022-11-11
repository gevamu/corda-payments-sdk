package com.gevamu.web.server.models;

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
}
