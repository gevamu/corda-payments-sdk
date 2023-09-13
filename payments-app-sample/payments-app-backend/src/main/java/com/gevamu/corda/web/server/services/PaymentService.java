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
import com.gevamu.corda.flows.PaymentInstruction;
import com.gevamu.corda.flows.PaymentInstructionFormat;
import com.gevamu.corda.states.Payment;
import com.gevamu.corda.web.server.config.Participant;
import com.gevamu.corda.web.server.models.ParticipantAccount;
import com.gevamu.corda.web.server.models.PaymentRequest;
import com.gevamu.corda.web.server.models.PaymentState;
import com.gevamu.corda.web.server.models.WirePaymentRequest;
import com.gevamu.corda.web.server.util.CompletableFutures;
import com.gevamu.corda.web.server.util.MoreCollectors;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletionStage;

@Service
public class PaymentService {
    @Autowired
    private transient CordaRpcClientService cordaRpcClientService;

    @Autowired
    private transient IdGeneratorService idGeneratorService;

    @Autowired
    private transient ParticipantService participantService;

    @Autowired
    private transient RegistrationService registrationService;

    @Autowired
    private transient XmlMarshallingService xmlMarshallingService;

    public @NotNull CompletionStage<Void> processPayment(@NonNull PaymentRequest paymentRequest) {
        try {
            registrationService.getRegistration()
                .orElseThrow(ParticipantNotRegisteredException::new);
            byte[] bytes = xmlMarshallingService.marshalPaymentRequest(toWireFormat(paymentRequest));
            PaymentInstruction paymentInstruction = new PaymentInstruction(PaymentInstructionFormat.ISO20022_V9_XML_UTF8, bytes);
            return cordaRpcClientService.executePaymentFlow(paymentInstruction);
        }
        catch (Exception e) {
            return CompletableFutures.failedStage(e);
        }
    }

    public @NotNull List<PaymentState> getPaymentStates() {
        return cordaRpcClientService.getPayments()
            .stream()
            .map(this::toPaymentState)
            .collect(MoreCollectors.toUnmodifiableList());
    }

    private @NotNull PaymentState toPaymentState(@NotNull Payment payment) {
        byte[] attachment = cordaRpcClientService.getAttachedPaymentInstruction(payment.getPaymentInstructionId());
        WirePaymentRequest wireRequest = xmlMarshallingService.unmarshalPaymentRequest(attachment);

        return PaymentState.builder()
            .paymentId(payment.getEndToEndId())
            .status(payment.getStatus())
            .updateTime(payment.getTimestamp())
            .creationTime(wireRequest.getCreDtTm().toGregorianCalendar().toInstant())
            .endToEndId(wireRequest.getEndToEndId())
            .amount(wireRequest.getAmount())
            .currency(wireRequest.getCreditor().getCurrency())
            .creditor(fromParticipant(wireRequest.getCreditor()))
            .debtor(fromParticipant(wireRequest.getDebtor()))
            .build();
    }

    private @NotNull WirePaymentRequest toWireFormat(@NotNull PaymentRequest paymentRequest)
        throws ParticipantNotRegisteredException
    {
        WirePaymentRequest wireRequest = new WirePaymentRequest();
        wireRequest.setParticipantId(
            registrationService.getRegistration()
                .map(ParticipantRegistration::getParticipantId)
                .orElseThrow(ParticipantNotRegisteredException::new)
        );
        wireRequest.setCreditor(participantService.getCreditor(paymentRequest.getCreditorAccount()));
        wireRequest.setDebtor(participantService.getDebtor(paymentRequest.getDebtorAccount()));
        wireRequest.setAmount(paymentRequest.getAmount());
        wireRequest.setMsgId(idGeneratorService.generateId());
        wireRequest.setInstrId(idGeneratorService.generateId());
        wireRequest.setPmtInfId(idGeneratorService.generateId());
        wireRequest.setEndToEndId(idGeneratorService.generateEndToEndId());
        wireRequest.setCreDtTm(xmlMarshallingService.xmlNow());
        wireRequest.setReqdExctnDt(xmlMarshallingService.xmlToday());
        return wireRequest;
    }

    private static @NotNull ParticipantAccount fromParticipant(@NotNull Participant participant) {
        return ParticipantAccount.builder()
            .accountId(participant.getAccount())
            .accountName(participant.getAccountName())
            .currency(participant.getCurrency())
            .build();
    }
}
