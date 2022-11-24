package com.gevamu.payments.app.workflows.services

import com.gevamu.iso20022.pain.BranchAndFinancialInstitutionIdentification6
import com.gevamu.iso20022.pain.CashAccount38
import com.gevamu.iso20022.pain.CustomerCreditTransferInitiationV09
import com.gevamu.iso20022.pain.DateAndDateTime2Choice
import com.gevamu.iso20022.pain.GroupHeader85
import com.gevamu.iso20022.pain.ObjectFactory
import com.gevamu.iso20022.pain.PartyIdentification135
import com.gevamu.iso20022.pain.PaymentMethod3Code
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Account
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Creditor
import com.gevamu.payments.app.contracts.schemas.AccountSchemaV1.Debtor
import java.math.BigDecimal
import java.time.LocalDate
import java.util.GregorianCalendar
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class PaymentInstructionBuilderService(
    private val serviceHub: AppServiceHub
) : SingletonSerializeAsToken() {

    private val idGeneratorService: IdGeneratorService
        get() = serviceHub.cordaService(IdGeneratorService::class.java)

    private val registrationService: RegistrationService
        get() = serviceHub.cordaService(RegistrationService::class.java)

    private val objectFactory = ObjectFactory()
    private val datatypeFactory = DatatypeFactory.newInstance()

    fun buildPaymentInstruction(amount: BigDecimal, debtor: Debtor, creditor: Creditor): CustomerCreditTransferInitiationV09 {
        val creditorIdentification = getParticipantIdentification(creditor)
        val debtorIdentification = getParticipantIdentification(debtor)

        val result = objectFactory.createCustomerCreditTransferInitiationV09()
        result.grpHdr = createGroupHeader(debtorIdentification)

        val cdtTrfTxInf = objectFactory.createCreditTransferTransaction34()
        cdtTrfTxInf.cdtr = creditorIdentification.partyIdentification
        cdtTrfTxInf.cdtrAcct = creditorIdentification.cashAccount
        cdtTrfTxInf.cdtrAgt = creditorIdentification.branchAndFinancialInstitutionIdentification
        val currencyAndAmount = objectFactory.createActiveOrHistoricCurrencyAndAmount()
        currencyAndAmount.ccy = creditorIdentification.cashAccount.ccy
        currencyAndAmount.value = amount
        val amountType = objectFactory.createAmountType4Choice()
        amountType.instdAmt = currencyAndAmount
        cdtTrfTxInf.amt = amountType
        val paymentIdentification = objectFactory.createPaymentIdentification6()
        paymentIdentification.instrId = idGeneratorService.generateId()
        paymentIdentification.endToEndId = idGeneratorService.generateEndToEndId()
        cdtTrfTxInf.pmtId = paymentIdentification

        val purp = objectFactory.createPurpose2Choice()
        purp.cd = "CGODDR"
        cdtTrfTxInf.purp = purp
        val rmtInf = objectFactory.createRemittanceInformation16()
        rmtInf.ustrd.add("2037123 IT test")
        cdtTrfTxInf.rmtInf = rmtInf
        cdtTrfTxInf.instrForDbtrAgt = "Instr For Debtor Agent"
        val instrForCdtrAgt = objectFactory.createInstructionForCreditorAgent1()
        instrForCdtrAgt.instrInf = "ACC/SERVICE TRADE"
        cdtTrfTxInf.instrForCdtrAgt.add(instrForCdtrAgt)

        val pmtInf = objectFactory.createPaymentInstruction30()
        pmtInf.pmtInfId = idGeneratorService.generateId()
        pmtInf.pmtMtd = PaymentMethod3Code.TRF
        pmtInf.dbtr = debtorIdentification.partyIdentification
        pmtInf.dbtrAcct = debtorIdentification.cashAccount
        pmtInf.dbtrAgt = debtorIdentification.branchAndFinancialInstitutionIdentification
        pmtInf.cdtTrfTxInf.add(cdtTrfTxInf)
        val reqdExctnDt = DateAndDateTime2Choice()
        reqdExctnDt.dt = today()
        pmtInf.reqdExctnDt = reqdExctnDt

        result.pmtInf.add(pmtInf)

        return result
    }

    private fun getParticipantIdentification(creditor: Creditor): ParticipantIdentification {
        return createParticipantIdentification(creditor.account!!)
    }

    private fun getParticipantIdentification(debtor: Debtor): ParticipantIdentification {
        val identification = createParticipantIdentification(debtor.account!!)

        val genericOrgId = objectFactory.createGenericOrganisationIdentification1()
        genericOrgId.id = registrationService.getRegistration()?.participantId
        val orgId = objectFactory.createOrganisationIdentification29()
        orgId.othr.add(genericOrgId)
        val id = objectFactory.createParty38Choice()
        id.orgId = orgId
        identification.partyIdentification.id = id

        return identification
    }

    private fun createParticipantIdentification(account: Account): ParticipantIdentification {
        val branchAndFinancialInstitutionIdentification: BranchAndFinancialInstitutionIdentification6 =
            createBranchAndFinancialInstitutionIdentification(account)
        val partyIdentification: PartyIdentification135 = createPartyIdentification(account)
        val cashAccount: CashAccount38 = createCashAccount(account)
        return ParticipantIdentification(
            branchAndFinancialInstitutionIdentification,
            partyIdentification,
            cashAccount
        )
    }

    private fun createBranchAndFinancialInstitutionIdentification(account: Account): BranchAndFinancialInstitutionIdentification6 {
        val financialInstitutionIdentification = objectFactory.createFinancialInstitutionIdentification18()
        financialInstitutionIdentification.bicfi = account.bic
        val result = objectFactory.createBranchAndFinancialInstitutionIdentification6()
        result.finInstnId = financialInstitutionIdentification
        return result
    }

    private fun createPartyIdentification(account: Account): PartyIdentification135 {
        val result = objectFactory.createPartyIdentification135()
        result.nm = account.accountName
        val pstlAdr = objectFactory.createPostalAddress24()
        pstlAdr.ctry = account.country!!.isoCodeAlpha2
        result.pstlAdr = pstlAdr
        return result
    }

    private fun createCashAccount(account: Account): CashAccount38 {
        val othr = objectFactory.createGenericAccountIdentification1()
        othr.id = account.account
        val id = objectFactory.createAccountIdentification4Choice()
        id.othr = othr
        val result = objectFactory.createCashAccount38()
        result.id = id
        result.nm = account.accountName
        result.ccy = account.currency!!.isoCode
        return result
    }

    private fun createGroupHeader(debtorIdentification: ParticipantIdentification): GroupHeader85 {
        val now = now()
        val grpHdr = objectFactory.createGroupHeader85()
        val partyId = objectFactory.createPartyIdentification135()
        partyId.nm = debtorIdentification.partyIdentification.nm
        grpHdr.msgId = idGeneratorService.generateId()
        grpHdr.creDtTm = now
        grpHdr.nbOfTxs = "1"
        grpHdr.initgPty = partyId
        return grpHdr
    }

    private fun now(): XMLGregorianCalendar? {
        return datatypeFactory.newXMLGregorianCalendar(GregorianCalendar())
    }

    private fun today(): XMLGregorianCalendar? {
        val today = LocalDate.now()
        return datatypeFactory.newXMLGregorianCalendar(today.toString())
    }
}

class ParticipantIdentification(
    val branchAndFinancialInstitutionIdentification: BranchAndFinancialInstitutionIdentification6,
    val partyIdentification: PartyIdentification135,
    val cashAccount: CashAccount38
)
