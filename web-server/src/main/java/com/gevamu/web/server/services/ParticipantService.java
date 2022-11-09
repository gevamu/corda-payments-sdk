package com.gevamu.web.server.services;

import com.gevamu.iso20022.pain.BranchAndFinancialInstitutionIdentification6;
import com.gevamu.iso20022.pain.CashAccount38;
import com.gevamu.iso20022.pain.ObjectFactory;
import com.gevamu.iso20022.pain.PartyIdentification135;
import com.gevamu.web.server.config.Participant;
import com.gevamu.web.server.config.Participants;
import lombok.NonNull;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final transient Map<String, Participant> creditors;
    private final transient Map<String, Participant> debtors;

    public ParticipantService(Participants participants) {
        creditors = participants.getCreditors()
            .stream()
            .collect(Collectors.toUnmodifiableMap(Participant::getAccount, Function.identity()));
        debtors = participants.getDebtors()
            .stream()
            .collect(Collectors.toUnmodifiableMap(Participant::getAccount, Function.identity()));
    }

    public Collection<Participant> getCreditors() {
        return creditors.values();
    }

    public ParticipantIdentification getCreditorIdentification(@NonNull String account) {
        var creditor = creditors.get(account);
        return createIdentification(creditor);
    }

    public ParticipantIdentification getDebtorIdentification(@NonNull String account) {
        var debtor = debtors.get(account);
        return createIdentification(debtor);
    }

    //FIXME temporary
    public ParticipantIdentification getDefaultDebtorIdentification() {
        var registration = registrationService.getRegistration()
            .orElseThrow(ParticipantNotRegisteredException::new);

        var debtor = debtors.values()
            .stream()
            .findFirst()
            .orElseThrow(NoSuchElementException::new);

        var identification = createIdentification(debtor);

        var genericOrgId = objectFactory.createGenericOrganisationIdentification1();
        genericOrgId.setId(registration.getParticipantId());
        var orgId = objectFactory.createOrganisationIdentification29();
        orgId.getOthr().add(genericOrgId);
        var id = objectFactory.createParty38Choice();
        id.setOrgId(orgId);
        identification.getPartyIdentification().setId(id);

        return identification;
    }

    private ParticipantIdentification createIdentification(@NonNull Participant participant) {
        var branchAndFinancialInstitutionIdentification = createBranchAndFinancialInstitutionIdentification(participant);
        var partyIdentification = createPartyIdentification(participant);
        var cashAccount = createCashAccount(participant);
        return new ParticipantIdentification(branchAndFinancialInstitutionIdentification, partyIdentification, cashAccount);
    }

    private BranchAndFinancialInstitutionIdentification6 createBranchAndFinancialInstitutionIdentification(Participant participant) {
        var financialInstitutionIdentification = objectFactory.createFinancialInstitutionIdentification18();
        financialInstitutionIdentification.setBICFI(participant.getBic());
        var result = objectFactory.createBranchAndFinancialInstitutionIdentification6();
        result.setFinInstnId(financialInstitutionIdentification);
        return result;
    }

    private PartyIdentification135 createPartyIdentification(Participant participant) {
        var result = objectFactory.createPartyIdentification135();
        result.setNm(participant.getAccountName());

        var pstlAdr = objectFactory.createPostalAddress24();
        pstlAdr.setCtry(participant.getCountry());
        result.setPstlAdr(pstlAdr);

        return result;
    }

    private CashAccount38 createCashAccount(Participant participant) {
        var othr = objectFactory.createGenericAccountIdentification1();
        othr.setId(participant.getAccount());

        var id = objectFactory.createAccountIdentification4Choice();
        id.setOthr(othr);

        var result = objectFactory.createCashAccount38();
        result.setId(id);
        result.setNm(participant.getAccountName());
        result.setCcy(participant.getCurrency());
        return result;
    }
}
