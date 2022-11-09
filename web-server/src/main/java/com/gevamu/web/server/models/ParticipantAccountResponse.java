package com.gevamu.web.server.models;

import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
public class ParticipantAccountResponse {
    @NonNull
    List<ParticipantAccount> accounts;
}
