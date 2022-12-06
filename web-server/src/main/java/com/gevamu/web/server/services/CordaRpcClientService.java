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

import com.gevamu.flows.ParticipantRegistration;
import com.gevamu.flows.PaymentFlow;
import com.gevamu.flows.PaymentInstruction;
import com.gevamu.flows.RegisterParticipantFlow;
import com.gevamu.states.Payment;
import com.gevamu.web.server.config.CordaRpcClientConnection;
import com.gevamu.web.server.util.MoreCollectors;
import lombok.NonNull;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.CordaRuntimeException;
import net.corda.core.crypto.SecureHash;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletionStage;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.validation.constraints.NotBlank;

@Service
public class CordaRpcClientService implements AutoCloseable {

    private static final String GATEWAY_PARTY_NAME = "Gateway";
    private static final String PAYMENT_INSTRUCTION_ATTACHMENT = "paymentInstruction.xml";

    private final transient CordaRPCConnection connection;
    private final transient CordaRPCOps proxy;

    public CordaRpcClientService(CordaRpcClientConnection cordaRpcClientConnection) {
        NetworkHostAndPort networkAddress = new NetworkHostAndPort(cordaRpcClientConnection.getHost(), cordaRpcClientConnection.getPort());
        CordaRPCClient client = new CordaRPCClient(networkAddress);
        connection = client.start(cordaRpcClientConnection.getUser(), cordaRpcClientConnection.getPassword());
        proxy = connection.getProxy();
    }

    @Override
    public void close() {
        connection.notifyServerAndClose();
    }

    public Party getParty(@NotBlank String partyName) {
        return proxy.partiesFromName(partyName, true)
            .stream()
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Party not found: " + partyName));
    }

    public CompletionStage<Void> executePaymentFlow(@NonNull PaymentInstruction paymentInstruction) {
        Party gatewayParty = getParty(GATEWAY_PARTY_NAME);
        return proxy.startFlowDynamic(PaymentFlow.class, paymentInstruction, gatewayParty)
            .getReturnValue()
            .toCompletableFuture()
            .thenApply(it -> null);
    }

    public CompletionStage<ParticipantRegistration> executeRegistrationFlow() {
        Party gatewayParty = getParty(GATEWAY_PARTY_NAME);
        return proxy.startFlowDynamic(RegisterParticipantFlow.class, gatewayParty)
            .getReturnValue()
            .toCompletableFuture();
    }

    public List<Payment> getPayments() {
        return proxy.vaultQuery(Payment.class)
            .getStates()
            .stream()
            .map(it -> it.getState().getData())
            .collect(MoreCollectors.toUnmodifiableList());
    }

    public byte[] getAttachedPaymentInstruction(@NonNull SecureHash attachmentId) {
        try (
            InputStream inputStream = proxy.openAttachment(attachmentId);
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            for (;;) {
                ZipEntry entry = zipInputStream.getNextEntry();
                if (entry == null) {
                    break;
                }
                if (entry.isDirectory() || !PAYMENT_INSTRUCTION_ATTACHMENT.equals(entry.getName())) {
                    continue;
                }
                byte[] buffer = new byte[1024];
                int length;
                do {
                    length = zipInputStream.read(buffer);
                    if (length > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                } while (length > 0);
            }
            return outputStream.toByteArray();
        }
        catch (Exception e) {
            throw new CordaRuntimeException("Payment instruction reading error", e);
        }
    }
}
