// Copyright 2022 Exactpro Systems Limited
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.gevamu.web.server.services;

import com.gevamu.iso20022.pain.ActiveOrHistoricCurrencyAndAmount;
import com.gevamu.iso20022.pain.AmountType4Choice;
import com.gevamu.iso20022.pain.CreditTransferTransaction34;
import com.gevamu.iso20022.pain.CustomerCreditTransferInitiationV09;
import com.gevamu.iso20022.pain.DateAndDateTime2Choice;
import com.gevamu.iso20022.pain.GroupHeader85;
import com.gevamu.iso20022.pain.InstructionForCreditorAgent1;
import com.gevamu.iso20022.pain.ObjectFactory;
import com.gevamu.iso20022.pain.PartyIdentification135;
import com.gevamu.iso20022.pain.PaymentIdentification6;
import com.gevamu.iso20022.pain.PaymentInstruction30;
import com.gevamu.iso20022.pain.PaymentMethod3Code;
import com.gevamu.iso20022.pain.Purpose2Choice;
import com.gevamu.iso20022.pain.RemittanceInformation16;
import com.gevamu.web.server.models.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

@Service
public class CustomerCreditTransferInitiationService {

    @Autowired
    private transient ParticipantService participantService;

    @Autowired
    private transient IdGeneratorService idGeneratorService;

    private final transient DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
    private final transient ObjectFactory objectFactory = new ObjectFactory();

    public CustomerCreditTransferInitiationService() throws DatatypeConfigurationException {
    }

    public CustomerCreditTransferInitiationV09 createCustomerCreditTransferInitiation(PaymentRequest paymentRequest) {

        ParticipantService.ParticipantIdentification creditorIdentification = participantService.getCreditorIdentification(paymentRequest.getCreditorAccount());
        ParticipantService.ParticipantIdentification debtorIdentification = participantService.getDebtorIdentification(paymentRequest.getDebtorAccount());

        CustomerCreditTransferInitiationV09 result = objectFactory.createCustomerCreditTransferInitiationV09();
        setGroupHeader(result, debtorIdentification, paymentRequest);

        CreditTransferTransaction34 cdtTrfTxInf = objectFactory.createCreditTransferTransaction34();
        cdtTrfTxInf.setCdtr(creditorIdentification.getPartyIdentification());
        cdtTrfTxInf.setCdtrAcct(creditorIdentification.getCashAccount());
        cdtTrfTxInf.setCdtrAgt(creditorIdentification.getBranchAndFinancialInstitutionIdentification());
        ActiveOrHistoricCurrencyAndAmount currencyAndAmount = objectFactory.createActiveOrHistoricCurrencyAndAmount();
        currencyAndAmount.setCcy(creditorIdentification.getCashAccount().getCcy());
        currencyAndAmount.setValue(paymentRequest.getAmount());
        AmountType4Choice amount = objectFactory.createAmountType4Choice();
        amount.setInstdAmt(currencyAndAmount);
        cdtTrfTxInf.setAmt(amount);
        PaymentIdentification6 paymentIdentification = objectFactory.createPaymentIdentification6();
        paymentIdentification.setInstrId(idGeneratorService.generateId());
        paymentIdentification.setEndToEndId(idGeneratorService.generateEndToEndId());
        cdtTrfTxInf.setPmtId(paymentIdentification);

        Purpose2Choice purp = objectFactory.createPurpose2Choice();
        purp.setCd("CGODDR");
        cdtTrfTxInf.setPurp(purp);
        RemittanceInformation16 rmtInf = objectFactory.createRemittanceInformation16();
        rmtInf.getUstrd().add("2037123 IT test");
        cdtTrfTxInf.setRmtInf(rmtInf);
        cdtTrfTxInf.setInstrForDbtrAgt("Instr For Debtor Agent");
        InstructionForCreditorAgent1 instrForCdtrAgt = objectFactory.createInstructionForCreditorAgent1();
        instrForCdtrAgt.setInstrInf("ACC/SERVICE TRADE");
        cdtTrfTxInf.getInstrForCdtrAgt().add(instrForCdtrAgt);

        PaymentInstruction30 pmtInf = objectFactory.createPaymentInstruction30();
        pmtInf.setPmtInfId(idGeneratorService.generateId());
        pmtInf.setPmtMtd(PaymentMethod3Code.TRF);
        pmtInf.setDbtr(debtorIdentification.getPartyIdentification());
        pmtInf.setDbtrAcct(debtorIdentification.getCashAccount());
        pmtInf.setDbtrAgt(debtorIdentification.getBranchAndFinancialInstitutionIdentification());
        pmtInf.getCdtTrfTxInf().add(cdtTrfTxInf);
        DateAndDateTime2Choice reqdExctnDt = new DateAndDateTime2Choice();
        reqdExctnDt.setDt(today());
        pmtInf.setReqdExctnDt(reqdExctnDt);

        result.getPmtInf().add(pmtInf);

        return result;
    }

    private void setGroupHeader(CustomerCreditTransferInitiationV09 target, ParticipantService.ParticipantIdentification debtorIdentification, PaymentRequest paymentRequest) {
        XMLGregorianCalendar now = now();
        GroupHeader85 grpHdr = objectFactory.createGroupHeader85();
        PartyIdentification135 partyId = objectFactory.createPartyIdentification135();
        partyId.setNm(debtorIdentification.getPartyIdentification().getNm());
        grpHdr.setMsgId(idGeneratorService.generateId());
        grpHdr.setCreDtTm(now);
        grpHdr.setNbOfTxs("1");
        grpHdr.setInitgPty(partyId);
        target.setGrpHdr(grpHdr);
    }

    private XMLGregorianCalendar now() {
        return datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());
    }

    private XMLGregorianCalendar today() {
        LocalDate today = LocalDate.now();
        return datatypeFactory.newXMLGregorianCalendar(today.toString());
    }
}
