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

import com.gevamu.iso20022.pain.AccountIdentification4Choice;
import com.gevamu.iso20022.pain.BranchAndFinancialInstitutionIdentification6;
import com.gevamu.iso20022.pain.CashAccount38;
import com.gevamu.iso20022.pain.FinancialInstitutionIdentification18;
import com.gevamu.iso20022.pain.GenericAccountIdentification1;
import com.gevamu.iso20022.pain.GenericOrganisationIdentification1;
import com.gevamu.iso20022.pain.ObjectFactory;
import com.gevamu.iso20022.pain.OrganisationIdentification29;
import com.gevamu.iso20022.pain.Party38Choice;
import com.gevamu.iso20022.pain.PartyIdentification135;
import com.gevamu.iso20022.pain.PostalAddress24;
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1;
import com.gevamu.payments.app.workflows.flows.CreditorRetrievalFlow;
import com.gevamu.payments.app.workflows.flows.DebtorRetrievalFlow;
import com.gevamu.web.server.config.Participant;
import lombok.NonNull;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Service
public class ParticipantService {

    @Autowired
    private transient RegistrationService registrationService;

    @Value
    public static class ParticipantIdentification {
        BranchAndFinancialInstitutionIdentification6 branchAndFinancialInstitutionIdentification;
        PartyIdentification135 partyIdentification;
        CashAccount38 cashAccount;
    }

    private final transient ObjectFactory objectFactory = new ObjectFactory();
    private final transient Map<String, Participant> creditors = Collections.emptyMap();
    private final transient Map<String, Participant> debtors = Collections.emptyMap();;

    @Autowired
    private transient CordaRpcClientService cordaRpcClientService;

    public CompletionStage<List<? extends AccountSchemaV1.Account>> getCreditors() {
        return cordaRpcClientService.executeFlow(CreditorRetrievalFlow.class);
    }

    public CompletionStage<List<? extends AccountSchemaV1.Account>> getDebtors() {
        return cordaRpcClientService.executeFlow(DebtorRetrievalFlow.class);
    }

    public ParticipantIdentification getCreditorIdentification(@NonNull String account) {
        Participant creditor = creditors.get(account);
        return createIdentification(creditor);
    }

    public ParticipantIdentification getDebtorIdentification(@NonNull String account) {
        return registrationService.getRegistration().blockOptional()
            .map(it -> {
                Participant debtor = debtors.get(account);
                ParticipantIdentification identification = createIdentification(debtor);

                GenericOrganisationIdentification1 genericOrgId = objectFactory.createGenericOrganisationIdentification1();
                genericOrgId.setId(it.getParticipantId());
                OrganisationIdentification29 orgId = objectFactory.createOrganisationIdentification29();
                orgId.getOthr().add(genericOrgId);
                Party38Choice id = objectFactory.createParty38Choice();
                id.setOrgId(orgId);
                identification.getPartyIdentification().setId(id);

                return identification;
            })
            .orElseThrow(ParticipantNotRegisteredException::new);
    }

    private ParticipantIdentification createIdentification(@NonNull Participant participant) {
        BranchAndFinancialInstitutionIdentification6 branchAndFinancialInstitutionIdentification = createBranchAndFinancialInstitutionIdentification(participant);
        PartyIdentification135 partyIdentification = createPartyIdentification(participant);
        CashAccount38 cashAccount = createCashAccount(participant);
        return new ParticipantIdentification(branchAndFinancialInstitutionIdentification, partyIdentification, cashAccount);
    }

    private BranchAndFinancialInstitutionIdentification6 createBranchAndFinancialInstitutionIdentification(Participant participant) {
        FinancialInstitutionIdentification18 financialInstitutionIdentification = objectFactory.createFinancialInstitutionIdentification18();
        financialInstitutionIdentification.setBICFI(participant.getBic());
        BranchAndFinancialInstitutionIdentification6 result = objectFactory.createBranchAndFinancialInstitutionIdentification6();
        result.setFinInstnId(financialInstitutionIdentification);
        return result;
    }

    private PartyIdentification135 createPartyIdentification(Participant participant) {
        PartyIdentification135 result = objectFactory.createPartyIdentification135();
        result.setNm(participant.getAccountName());

        PostalAddress24 pstlAdr = objectFactory.createPostalAddress24();
        pstlAdr.setCtry(participant.getCountry());
        result.setPstlAdr(pstlAdr);

        return result;
    }

    private CashAccount38 createCashAccount(Participant participant) {
        GenericAccountIdentification1 othr = objectFactory.createGenericAccountIdentification1();
        othr.setId(participant.getAccount());

        AccountIdentification4Choice id = objectFactory.createAccountIdentification4Choice();
        id.setOthr(othr);

        CashAccount38 result = objectFactory.createCashAccount38();
        result.setId(id);
        result.setNm(participant.getAccountName());
        result.setCcy(participant.getCurrency());
        return result;
    }
}
