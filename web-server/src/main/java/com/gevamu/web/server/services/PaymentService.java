package com.gevamu.web.server.services;

import com.gevamu.flows.PaymentInstruction;
import com.gevamu.flows.PaymentInstructionFormat;
import com.gevamu.iso20022.pain.CreditTransferTransaction34;
import com.gevamu.iso20022.pain.CustomerCreditTransferInitiationV09;
import com.gevamu.iso20022.pain.PaymentInstruction30;
import com.gevamu.states.Payment;
import com.gevamu.web.server.models.ParticipantAccount;
import com.gevamu.web.server.models.PaymentRequest;
import com.gevamu.web.server.models.PaymentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private transient RegistrationService registrationService;

    @Autowired
    private transient CordaRpcClientService cordaRpcClientService;

    @Autowired
    private transient CustomerCreditTransferInitiationService customerCreditTransferInitiationService;

    @Autowired
    private transient XmlMarshallingService xmlMarshallingService;

    public CompletionStage<Void> processPayment(PaymentRequest paymentRequest) {
        try {
            registrationService.getRegistration()
                .orElseThrow(ParticipantNotRegisteredException::new);
            var customerCreditTransferInitiation = customerCreditTransferInitiationService.createCustomerCreditTransferInitiation(paymentRequest);
            var bytes = xmlMarshallingService.marshal(customerCreditTransferInitiation);
            var paymentInstruction = new PaymentInstruction(PaymentInstructionFormat.ISO20022_V9_XML_UTF8, bytes);
            return cordaRpcClientService.executePaymentFlow(paymentInstruction);
        }
        catch (Exception e) {
            return CompletableFuture.failedStage(e);
        }
    }

    public List<PaymentState> getPaymentStates() {
        return cordaRpcClientService.getPayments()
            .stream()
            .map(this::convert)
            .collect(Collectors.toUnmodifiableList());
    }

    private PaymentState convert(Payment payment) {
        var builder = PaymentState.builder()
            .paymentId(payment.getLinearId().toString())
            .status(payment.getStatus())
            .updateTime(payment.getTimestamp());

        var attachment = cordaRpcClientService.getAttachedPaymentInstruction(payment.getPaymentInstructionId());
        CustomerCreditTransferInitiationV09 xml = xmlMarshallingService.unmarshal(attachment);
        var creationTime = xml.getGrpHdr()
            .getCreDtTm()
            .toGregorianCalendar()
            .toZonedDateTime()
            .toInstant();
        builder.creationTime(creationTime);

        if (!xml.getPmtInf().isEmpty()) {
            var paymentInstruction = xml.getPmtInf().get(0);
            if (!paymentInstruction.getCdtTrfTxInf().isEmpty()) {
                var transaction = paymentInstruction.getCdtTrfTxInf().get(0);
                var endToEndId = transaction.getPmtId().getEndToEndId();
                var amount = transaction.getAmt().getInstdAmt().getValue();
                var currency = transaction.getAmt().getInstdAmt().getCcy();
                var creditor = deriveCreditor(transaction);
                var debtor = deriveDebtor(paymentInstruction);
                builder.endToEndId(endToEndId)
                    .amount(amount)
                    .currency(currency)
                    .creditor(creditor)
                    .debtor(debtor);
            }
        }

        return builder.build();
    }

    private ParticipantAccount deriveCreditor(CreditTransferTransaction34 transaction) {
        var creditor = transaction.getCdtr().getNm();
        var creditorAccount = transaction.getCdtrAcct().getId().getOthr().getId();
        var creditorCurrency = transaction.getCdtrAcct().getCcy();
        return ParticipantAccount.builder()
            .accountId(creditorAccount)
            .accountName(creditor)
            .currency(creditorCurrency)
            .build();
    }

    private ParticipantAccount deriveDebtor(PaymentInstruction30 paymentInstruction) {
        var creditor = paymentInstruction.getDbtr().getNm();
        var creditorAccount = paymentInstruction.getDbtrAcct().getId().getOthr().getId();
        var creditorCurrency = paymentInstruction.getDbtrAcct().getCcy();
        return ParticipantAccount.builder()
            .accountId(creditorAccount)
            .accountName(creditor)
            .currency(creditorCurrency)
            .build();
    }
}
