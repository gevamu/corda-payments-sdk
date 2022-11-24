package com.gevamu.web.server.services;

import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1;
import com.gevamu.payments.app.workflows.flows.CreditorRetrievalFlow;
import com.gevamu.payments.app.workflows.flows.DebtorRetrievalFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletionStage;

@Service
public class ParticipantService {

    @Autowired
    private transient CordaRpcClientService cordaRpcClientService;

    public CompletionStage<List<? extends AccountSchemaV1.Account>> getCreditors() {
        return cordaRpcClientService.executeFlow(CreditorRetrievalFlow.class);
    }

    public CompletionStage<List<? extends AccountSchemaV1.Account>> getDebtors() {
        return cordaRpcClientService.executeFlow(DebtorRetrievalFlow.class);
    }
}
