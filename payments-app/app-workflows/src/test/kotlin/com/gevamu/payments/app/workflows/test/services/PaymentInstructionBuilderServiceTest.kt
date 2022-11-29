package com.gevamu.payments.app.workflows.test.services

import SpyServices
import com.gevamu.flows.ParticipantRegistration
import com.gevamu.iso20022.pain.BranchAndFinancialInstitutionIdentification6
import com.gevamu.iso20022.pain.CashAccount38
import com.gevamu.iso20022.pain.CreditTransferTransaction34
import com.gevamu.iso20022.pain.GroupHeader85
import com.gevamu.iso20022.pain.PartyIdentification135
import com.gevamu.iso20022.pain.PaymentInstruction30
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Account
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Country
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Creditor
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Currency
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Debtor
import com.gevamu.payments.app.workflows.flows.PaymentInitiationRequest
import com.gevamu.payments.app.workflows.services.EntityManagerService
import com.gevamu.payments.app.workflows.services.PaymentInstructionBuilderService
import com.gevamu.payments.app.workflows.services.RegistrationService
import java.math.BigDecimal
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn

class PaymentInstructionBuilderServiceTest {

    private val services: SpyServices = SpyServices()
    private val paymentInstructionBuilderService: PaymentInstructionBuilderService =
        services.cordaService(PaymentInstructionBuilderService::class.java)

    @BeforeEach
    fun beforeEach() {
        val registration = ParticipantRegistration("test_p_id", "test_n_id")
        val registrationService = services.cordaService(RegistrationService::class.java)
        doReturn(registration).`when`(registrationService).getRegistration()

        val creditor = createCreditor()
        val debtor = createDebtor()
        val entityManagerService = services.cordaService(EntityManagerService::class.java)
        doReturn(creditor).`when`(entityManagerService).getCreditor(creditor.account!!.account!!)
        doReturn(debtor).`when`(entityManagerService).getDebtor(debtor.account!!.account!!)
        doReturn(creditor.account!!.currency!!).`when`(entityManagerService).getCurrency(creditor.account!!.currency!!.isoCode!!)
        doReturn(debtor.account!!.currency!!).`when`(entityManagerService).getCurrency(debtor.account!!.currency!!.isoCode!!)
    }

    @Test
    fun test() {
        val request = PaymentInitiationRequest("test_creditor_account", "test_debtor_account", BigDecimal.TEN)
        val result = paymentInstructionBuilderService.buildPaymentInstruction(request)

        assertThat(result).isNotNull

        validateGrpHdr(result.grpHdr)

        assertThat(result.pmtInf.size).isEqualTo(1)
        val payment = result.pmtInf[0]
        validatePaymentInstruction(payment)

        val debtor = payment.dbtr
        validateDebtor(debtor)

        val debtorAcct = payment.dbtrAcct
        validateDebtorAcct(debtorAcct)

        val debtorAgt = payment.dbtrAgt
        validateDebtorAgt(debtorAgt)

        val transactions = payment.cdtTrfTxInf
        assertThat(transactions.size).isEqualTo(1)
        val transaction = transactions[0]
        validateTransaction(transaction)
    }

    private fun validateGrpHdr(grpHdr: GroupHeader85) {
        assertThat(grpHdr).isNotNull
        assertThat(StringUtils.isAlphanumeric(grpHdr.msgId)).isTrue
        assertThat(grpHdr.creDtTm).isNotNull
        assertThat(grpHdr.nbOfTxs).isEqualTo("1")
        assertThat(grpHdr.initgPty).isNotNull
        assertThat(grpHdr.initgPty.nm).isEqualTo("test_debtor_accountName")
    }

    private fun validatePaymentInstruction(payment: PaymentInstruction30) {
        assertThat(payment).isNotNull
        assertThat(StringUtils.isAlphanumeric(payment.pmtInfId)).isTrue
        assertThat(payment.pmtMtd).isNotNull
        assertThat(payment.pmtMtd.value()).isEqualTo("TRF")
        assertThat(payment.reqdExctnDt).isNotNull
        assertThat(payment.reqdExctnDt.dt).isNotNull
    }

    private fun validateDebtor(debtor: PartyIdentification135) {
        assertThat(debtor).isNotNull
        assertThat(debtor.nm).isEqualTo("test_debtor_accountName")
        assertThat(debtor.pstlAdr).isNotNull
        assertThat(debtor.pstlAdr.ctry).isEqualTo("test_debtor_country")
        assertThat(debtor.id).isNotNull
        assertThat(debtor.id.orgId).isNotNull
        assertThat(debtor.id.orgId.othr.size).isEqualTo(1)
        val debtorOrg = debtor.id.orgId.othr[0]
        assertThat(debtorOrg).isNotNull
        assertThat(debtorOrg.id).isEqualTo("test_p_id")
    }

    private fun validateDebtorAcct(debtorAcct: CashAccount38) {
        assertThat(debtorAcct).isNotNull
        assertThat(debtorAcct.id).isNotNull
        assertThat(debtorAcct.id.othr).isNotNull
        assertThat(debtorAcct.id.othr.id).isEqualTo("test_debtor_account")
        assertThat(debtorAcct.ccy).isEqualTo("test_debtor_currency")
        assertThat(debtorAcct.nm).isEqualTo("test_debtor_accountName")
    }

    private fun validateDebtorAgt(debtorAgt: BranchAndFinancialInstitutionIdentification6) {
        assertThat(debtorAgt).isNotNull
        assertThat(debtorAgt.finInstnId).isNotNull
        assertThat(debtorAgt.finInstnId.bicfi).isEqualTo("test_debtor_bic")
    }

    private fun validateTransaction(transaction: CreditTransferTransaction34) {
        assertThat(transaction).isNotNull
        assertThat(transaction.pmtId).isNotNull
        assertThat(StringUtils.isAlphanumeric(transaction.pmtId.instrId)).isTrue
        assertThat(StringUtils.isAlphanumeric(transaction.pmtId.endToEndId)).isTrue
        assertThat(transaction.amt).isNotNull
        assertThat(transaction.amt.instdAmt.value).isEqualTo(BigDecimal.TEN)
        assertThat(transaction.amt.instdAmt.ccy).isEqualTo("test_creditor_currency")
        val creditorAgt = transaction.cdtrAgt
        assertThat(creditorAgt).isNotNull
        assertThat(creditorAgt.finInstnId).isNotNull
        assertThat(creditorAgt.finInstnId.bicfi).isEqualTo("test_creditor_bic")
        val creditor = transaction.cdtr
        assertThat(creditor).isNotNull
        assertThat(creditor.nm).isEqualTo("test_creditor_accountName")
        assertThat(creditor.pstlAdr).isNotNull
        assertThat(creditor.pstlAdr.ctry).isEqualTo("test_creditor_country")
        val creditorAcct = transaction.cdtrAcct
        assertThat(creditorAcct).isNotNull
        assertThat(creditorAcct.id).isNotNull
        assertThat(creditorAcct.id.othr).isNotNull
        assertThat(creditorAcct.id.othr.id).isEqualTo("test_creditor_account")
        assertThat(creditorAcct.ccy).isEqualTo("test_creditor_currency")
        assertThat(creditorAcct.nm).isEqualTo("test_creditor_accountName")

        //FIXME the following values are hard-coded in the service
        assertThat(transaction.instrForCdtrAgt.size).isEqualTo(1)
        assertThat(transaction.instrForCdtrAgt[0].instrInf).isEqualTo("ACC/SERVICE TRADE")
        assertThat(transaction.instrForDbtrAgt).isEqualTo("Instr For Debtor Agent")
        assertThat(transaction.purp).isNotNull
        assertThat(transaction.purp.cd).isEqualTo("CGODDR")
        assertThat(transaction.rmtInf).isNotNull
        assertThat(transaction.rmtInf.ustrd.size).isEqualTo(1)
        assertThat(transaction.rmtInf.ustrd[0]).isEqualTo("2037123 IT test")
    }

    private fun createCreditor(): Creditor {
        val account = Account()
        account.bic = "test_creditor_bic"
        account.country = Country("test_creditor_country")
        account.currency = Currency("test_creditor_currency")
        account.account = "test_creditor_account"
        account.accountName = "test_creditor_accountName"
        return Creditor(account)
    }

    private fun createDebtor(): Debtor {
        val account = Account()
        account.bic = "test_debtor_bic"
        account.country = Country("test_debtor_country")
        account.currency = Currency("test_debtor_currency")
        account.account = "test_debtor_account"
        account.accountName = "test_debtor_accountName"
        return Debtor(account)
    }
}
