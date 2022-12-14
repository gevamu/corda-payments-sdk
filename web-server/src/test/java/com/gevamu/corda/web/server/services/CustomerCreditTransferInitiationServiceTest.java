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

package com.gevamu.corda.web.server.services;

import com.gevamu.corda.flows.ParticipantRegistration;
import com.gevamu.corda.iso20022.pain.BranchAndFinancialInstitutionIdentification6;
import com.gevamu.corda.iso20022.pain.CashAccount38;
import com.gevamu.corda.iso20022.pain.CreditTransferTransaction34;
import com.gevamu.corda.iso20022.pain.CustomerCreditTransferInitiationV09;
import com.gevamu.corda.iso20022.pain.GenericOrganisationIdentification1;
import com.gevamu.corda.iso20022.pain.GroupHeader85;
import com.gevamu.corda.iso20022.pain.PartyIdentification135;
import com.gevamu.corda.iso20022.pain.PaymentInstruction30;
import com.gevamu.corda.web.server.models.PaymentRequest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.when;

@SpringBootTest(
    properties = {
        "participants.creditors[0].bic=test_creditor_bic",
        "participants.creditors[0].country=test_creditor_country",
        "participants.creditors[0].currency=test_creditor_currency",
        "participants.creditors[0].account=test_creditor_account",
        "participants.creditors[0].accountName=test_creditor_accountName",
        "participants.creditors[0].effectiveDate=test_creditor_effectiveDate",
        "participants.creditors[0].expiryDate=test_creditor_expiryDate",
        "participants.creditors[0].paymentLimit=test_creditor_paymentLimit",
        "participants.debtors[0].bic=test_debtor_bic",
        "participants.debtors[0].country=test_debtor_country",
        "participants.debtors[0].currency=test_debtor_currency",
        "participants.debtors[0].account=test_debtor_account",
        "participants.debtors[0].accountName=test_debtor_accountName",
        "participants.debtors[0].effectiveDate=test_debtor_effectiveDate",
        "participants.debtors[0].expiryDate=test_debtor_expiryDate",
        "participants.debtors[0].paymentLimit=test_debtor_paymentLimit"
    }
)
@ActiveProfiles("test")
public class CustomerCreditTransferInitiationServiceTest {

    @Autowired
    private transient CustomerCreditTransferInitiationService customerCreditTransferInitiationService;

    @MockBean
    private transient RegistrationService registrationService;

    @MockBean
    private transient CordaRpcClientService cordaRpcClientService;

    @BeforeEach
    public void beforeEach() {
        ParticipantRegistration registration = new ParticipantRegistration("test_p_id", "test_n_id");
        when(registrationService.getRegistration())
            .thenReturn(Optional.of(registration));
    }

    @AfterEach
    public void afterEach() {
        clearInvocations(registrationService);
    }

    @Test
    public void test() {
        PaymentRequest request = new PaymentRequest("test_creditor_account", "test_debtor_account", BigDecimal.TEN);
        CustomerCreditTransferInitiationV09 result = customerCreditTransferInitiationService.createCustomerCreditTransferInitiation(request);
        assertThat(result).isNotNull();

        validateGrpHdr(result.getGrpHdr());

        assertThat(result.getPmtInf().size()).isEqualTo(1);
        PaymentInstruction30 payment = result.getPmtInf().get(0);
        validatePaymentInstruction(payment);

        PartyIdentification135 debtor = payment.getDbtr();
        validateDebtor(debtor);

        CashAccount38 debtorAcct = payment.getDbtrAcct();
        validateDebtorAcct(debtorAcct);

        BranchAndFinancialInstitutionIdentification6 debtorAgt = payment.getDbtrAgt();
        validateDebtorAgt(debtorAgt);

        List<CreditTransferTransaction34> transactions = payment.getCdtTrfTxInf();
        assertThat(transactions.size()).isEqualTo(1);
        CreditTransferTransaction34 transaction = transactions.get(0);
        validateTransaction(transaction);
    }

    private void validateGrpHdr(GroupHeader85 grpHdr) {
        assertThat(grpHdr).isNotNull();
        assertThat(StringUtils.isAlphanumeric(grpHdr.getMsgId())).isTrue();
        assertThat(grpHdr.getCreDtTm()).isNotNull();
        assertThat(grpHdr.getNbOfTxs()).isEqualTo("1");
        assertThat(grpHdr.getInitgPty()).isNotNull();
        assertThat(grpHdr.getInitgPty().getNm()).isEqualTo("test_debtor_accountName");
    }

    private void validatePaymentInstruction(PaymentInstruction30 payment) {
        assertThat(payment).isNotNull();
        assertThat(StringUtils.isAlphanumeric(payment.getPmtInfId())).isTrue();
        assertThat(payment.getPmtMtd()).isNotNull();
        assertThat(payment.getPmtMtd().value()).isEqualTo("TRF");
        assertThat(payment.getReqdExctnDt()).isNotNull();
        assertThat(payment.getReqdExctnDt().getDt()).isNotNull();
    }

    private void validateDebtor(PartyIdentification135 debtor) {
        assertThat(debtor).isNotNull();
        assertThat(debtor.getNm()).isEqualTo("test_debtor_accountName");
        assertThat(debtor.getPstlAdr()).isNotNull();
        assertThat(debtor.getPstlAdr().getCtry()).isEqualTo("test_debtor_country");
        assertThat(debtor.getId()).isNotNull();
        assertThat(debtor.getId().getOrgId()).isNotNull();
        assertThat(debtor.getId().getOrgId().getOthr().size()).isEqualTo(1);
        GenericOrganisationIdentification1 debtorOrg = debtor.getId().getOrgId().getOthr().get(0);
        assertThat(debtorOrg).isNotNull();
        assertThat(debtorOrg.getId()).isEqualTo("test_p_id");
    }

    private void validateDebtorAcct(CashAccount38 debtorAcct) {
        assertThat(debtorAcct).isNotNull();
        assertThat(debtorAcct.getId()).isNotNull();
        assertThat(debtorAcct.getId().getOthr()).isNotNull();
        assertThat(debtorAcct.getId().getOthr().getId()).isEqualTo("test_debtor_account");
        assertThat(debtorAcct.getCcy()).isEqualTo("test_debtor_currency");
        assertThat(debtorAcct.getNm()).isEqualTo("test_debtor_accountName");
    }

    private void validateDebtorAgt(BranchAndFinancialInstitutionIdentification6 debtorAgt) {
        assertThat(debtorAgt).isNotNull();
        assertThat(debtorAgt.getFinInstnId()).isNotNull();
        assertThat(debtorAgt.getFinInstnId().getBICFI()).isEqualTo("test_debtor_bic");
    }

    private void validateTransaction(CreditTransferTransaction34 transaction) {
        assertThat(transaction).isNotNull();
        assertThat(transaction.getPmtId()).isNotNull();
        assertThat(StringUtils.isAlphanumeric(transaction.getPmtId().getInstrId())).isTrue();
        assertThat(StringUtils.isAlphanumeric(transaction.getPmtId().getEndToEndId())).isTrue();

        assertThat(transaction.getAmt()).isNotNull();
        assertThat(transaction.getAmt().getInstdAmt().getValue()).isEqualTo(BigDecimal.TEN);
        assertThat(transaction.getAmt().getInstdAmt().getCcy()).isEqualTo("test_creditor_currency");

        BranchAndFinancialInstitutionIdentification6 creditorAgt = transaction.getCdtrAgt();
        assertThat(creditorAgt).isNotNull();
        assertThat(creditorAgt.getFinInstnId()).isNotNull();
        assertThat(creditorAgt.getFinInstnId().getBICFI()).isEqualTo("test_creditor_bic");

        PartyIdentification135 creditor = transaction.getCdtr();
        assertThat(creditor).isNotNull();
        assertThat(creditor.getNm()).isEqualTo("test_creditor_accountName");
        assertThat(creditor.getPstlAdr()).isNotNull();
        assertThat(creditor.getPstlAdr().getCtry()).isEqualTo("test_creditor_country");

        CashAccount38 creditorAcct = transaction.getCdtrAcct();
        assertThat(creditorAcct).isNotNull();
        assertThat(creditorAcct.getId()).isNotNull();
        assertThat(creditorAcct.getId().getOthr()).isNotNull();
        assertThat(creditorAcct.getId().getOthr().getId()).isEqualTo("test_creditor_account");
        assertThat(creditorAcct.getCcy()).isEqualTo("test_creditor_currency");
        assertThat(creditorAcct.getNm()).isEqualTo("test_creditor_accountName");

        //FIXME the following values are hard-coded in the service
        assertThat(transaction.getInstrForCdtrAgt().size()).isEqualTo(1);
        assertThat(transaction.getInstrForCdtrAgt().get(0).getInstrInf()).isEqualTo("ACC/SERVICE TRADE");
        assertThat(transaction.getInstrForDbtrAgt()).isEqualTo("Instr For Debtor Agent");
        assertThat(transaction.getPurp()).isNotNull();
        assertThat(transaction.getPurp().getCd()).isEqualTo("CGODDR");
        assertThat(transaction.getRmtInf()).isNotNull();
        assertThat(transaction.getRmtInf().getUstrd().size()).isEqualTo(1);
        assertThat(transaction.getRmtInf().getUstrd().get(0)).isEqualTo("2037123 IT test");
    }
}
