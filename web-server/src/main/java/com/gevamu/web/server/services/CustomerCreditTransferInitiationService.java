package com.gevamu.web.server.services;

import com.gevamu.iso20022.pain.CustomerCreditTransferInitiationV09;
import com.gevamu.iso20022.pain.DateAndDateTime2Choice;
import com.gevamu.iso20022.pain.ObjectFactory;
import com.gevamu.iso20022.pain.PaymentMethod3Code;
import com.gevamu.web.server.models.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.util.GregorianCalendar;

@Service
public class CustomerCreditTransferInitiationService {

    @Autowired
    private transient ParticipantService participantService;

    @Autowired
    private transient IdGeneratorService idGeneratorService;

    private final transient DatatypeFactory datatypeFactory = DatatypeFactory.newDefaultInstance();
    private final transient ObjectFactory objectFactory = new ObjectFactory();

    public CustomerCreditTransferInitiationV09 createCustomerCreditTransferInitiation(PaymentRequest paymentRequest) {

        var creditorIdentification = participantService.getCreditorIdentification(paymentRequest.getCreditorAccount());
        var debtorIdentification = participantService.getDebtorIdentification(paymentRequest.getDebtorAccount());

        var result = objectFactory.createCustomerCreditTransferInitiationV09();
        setGroupHeader(result, debtorIdentification, paymentRequest);

        var cdtTrfTxInf = objectFactory.createCreditTransferTransaction34();
        cdtTrfTxInf.setCdtr(creditorIdentification.getPartyIdentification());
        cdtTrfTxInf.setCdtrAcct(creditorIdentification.getCashAccount());
        cdtTrfTxInf.setCdtrAgt(creditorIdentification.getBranchAndFinancialInstitutionIdentification());
        var currencyAndAmount = objectFactory.createActiveOrHistoricCurrencyAndAmount();
        currencyAndAmount.setCcy(creditorIdentification.getCashAccount().getCcy());
        currencyAndAmount.setValue(paymentRequest.getAmount());
        var amount = objectFactory.createAmountType4Choice();
        amount.setInstdAmt(currencyAndAmount);
        cdtTrfTxInf.setAmt(amount);
        var paymentIdentification = objectFactory.createPaymentIdentification6();
        paymentIdentification.setInstrId(idGeneratorService.generateId());
        paymentIdentification.setEndToEndId(idGeneratorService.generateEndToEndId());
        cdtTrfTxInf.setPmtId(paymentIdentification);

        var purp = objectFactory.createPurpose2Choice();
        purp.setCd("CGODDR");
        cdtTrfTxInf.setPurp(purp);
        var rmtInf = objectFactory.createRemittanceInformation16();
        rmtInf.getUstrd().add("2037123 IT test");
        cdtTrfTxInf.setRmtInf(rmtInf);
        cdtTrfTxInf.setInstrForDbtrAgt("Instr For Debtor Agent");
        var instrForCdtrAgt = objectFactory.createInstructionForCreditorAgent1();
        instrForCdtrAgt.setInstrInf("ACC/SERVICE TRADE");
        cdtTrfTxInf.getInstrForCdtrAgt().add(instrForCdtrAgt);

        var pmtInf = objectFactory.createPaymentInstruction30();
        pmtInf.setPmtInfId(idGeneratorService.generateId());
        pmtInf.setPmtMtd(PaymentMethod3Code.TRF);
        pmtInf.setDbtr(debtorIdentification.getPartyIdentification());
        pmtInf.setDbtrAcct(debtorIdentification.getCashAccount());
        pmtInf.setDbtrAgt(debtorIdentification.getBranchAndFinancialInstitutionIdentification());
        pmtInf.getCdtTrfTxInf().add(cdtTrfTxInf);
        var reqdExctnDt = new DateAndDateTime2Choice();
        reqdExctnDt.setDt(today());
        pmtInf.setReqdExctnDt(reqdExctnDt);

        result.getPmtInf().add(pmtInf);

        return result;
    }

    private void setGroupHeader(CustomerCreditTransferInitiationV09 target, ParticipantService.ParticipantIdentification debtorIdentification, PaymentRequest paymentRequest) {
        var now = now();
        var grpHdr = objectFactory.createGroupHeader85();
        var partyId = objectFactory.createPartyIdentification135();
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
        var today = LocalDate.now();
        return datatypeFactory.newXMLGregorianCalendar(today.toString());
    }
}
