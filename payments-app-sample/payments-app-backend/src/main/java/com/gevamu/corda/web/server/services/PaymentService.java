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

import com.gevamu.corda.flows.PaymentInstruction;
import com.gevamu.corda.flows.PaymentInstructionFormat;
import com.gevamu.corda.iso20022.pain.CreditTransferTransaction34;
import com.gevamu.corda.iso20022.pain.CustomerCreditTransferInitiationV09;
import com.gevamu.corda.iso20022.pain.PaymentInstruction30;
import com.gevamu.corda.states.Payment;
import com.gevamu.corda.web.server.models.ParticipantAccount;
import com.gevamu.corda.web.server.models.PaymentRequest;
import com.gevamu.corda.web.server.models.PaymentState;
import com.gevamu.corda.web.server.util.CompletableFutures;
import com.gevamu.corda.web.server.util.MoreCollectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionStage;

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
            CustomerCreditTransferInitiationV09 customerCreditTransferInitiation = customerCreditTransferInitiationService.createCustomerCreditTransferInitiation(paymentRequest);
            byte[] bytes = xmlMarshallingService.marshal(customerCreditTransferInitiation);
            PaymentInstruction paymentInstruction = new PaymentInstruction(PaymentInstructionFormat.ISO20022_V9_XML_UTF8, bytes);
            return cordaRpcClientService.executePaymentFlow(paymentInstruction);
        }
        catch (Exception e) {
            return CompletableFutures.failedStage(e);
        }
    }

    public List<PaymentState> getPaymentStates() {
        return cordaRpcClientService.getPayments()
            .stream()
            .map(this::convert)
            .collect(MoreCollectors.toUnmodifiableList());
    }

    private PaymentState convert(Payment payment) {
        PaymentState.PaymentStateBuilder builder = PaymentState.builder()
            .paymentId(payment.getEndToEndId())
            .status(payment.getStatus())
            .updateTime(payment.getTimestamp());

        byte[] attachment = cordaRpcClientService.getAttachedPaymentInstruction(payment.getPaymentInstructionId());
        CustomerCreditTransferInitiationV09 xml = xmlMarshallingService.unmarshal(attachment);
        Instant creationTime = xml.getGrpHdr()
            .getCreDtTm()
            .toGregorianCalendar()
            .toZonedDateTime()
            .toInstant();
        builder.creationTime(creationTime);

        if (!xml.getPmtInf().isEmpty()) {
            PaymentInstruction30 paymentInstruction = xml.getPmtInf().get(0);
            if (!paymentInstruction.getCdtTrfTxInf().isEmpty()) {
                CreditTransferTransaction34 transaction = paymentInstruction.getCdtTrfTxInf().get(0);
                String endToEndId = transaction.getPmtId().getEndToEndId();
                BigDecimal amount = transaction.getAmt().getInstdAmt().getValue();
                String currency = transaction.getAmt().getInstdAmt().getCcy();
                ParticipantAccount creditor = deriveCreditor(transaction);
                ParticipantAccount debtor = deriveDebtor(paymentInstruction);
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
        String creditor = transaction.getCdtr().getNm();
        String creditorAccount = transaction.getCdtrAcct().getId().getOthr().getId();
        String creditorCurrency = transaction.getCdtrAcct().getCcy();
        return ParticipantAccount.builder()
            .accountId(creditorAccount)
            .accountName(creditor)
            .currency(creditorCurrency)
            .build();
    }

    private ParticipantAccount deriveDebtor(PaymentInstruction30 paymentInstruction) {
        String creditor = paymentInstruction.getDbtr().getNm();
        String creditorAccount = paymentInstruction.getDbtrAcct().getId().getOthr().getId();
        String creditorCurrency = paymentInstruction.getDbtrAcct().getCcy();
        return ParticipantAccount.builder()
            .accountId(creditorAccount)
            .accountName(creditor)
            .currency(creditorCurrency)
            .build();
    }
}
