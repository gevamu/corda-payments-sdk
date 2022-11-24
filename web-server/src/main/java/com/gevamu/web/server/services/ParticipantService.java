package com.gevamu.web.server.services;

import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletionStage;

@Service
public class ParticipantService {

    @Autowired
    private transient CordaRpcClientService cordaRpcClientService;

    public CompletionStage<List<? extends AccountSchemaV1.Account>> getCreditors() {
        return cordaRpcClientService.getCreditors();
    }

    public CompletionStage<List<? extends AccountSchemaV1.Account>> getDebtors() {
        return cordaRpcClientService.getDebtors();
    }
}
