package com.gevamu.web.server.services;

import com.gevamu.flows.ParticipantRegistration;
import com.gevamu.flows.PaymentFlow;
import com.gevamu.flows.PaymentInstruction;
import com.gevamu.flows.RegisterParticipantFlow;
import com.gevamu.states.Payment;
import com.gevamu.web.server.config.CordaRpcClientConnection;
import lombok.NonNull;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.CordaRuntimeException;
import net.corda.core.crypto.SecureHash;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

@Service
public class CordaRpcClientService implements AutoCloseable {

    private static final String GATEWAY_PARTY_NAME = "Gateway";
    private static final String PAYMENT_INSTRUCTION_ATTACHMENT = "paymentInstruction.xml";

    private final transient CordaRPCConnection connection;
    private final transient CordaRPCOps proxy;

    public CordaRpcClientService(CordaRpcClientConnection cordaRpcClientConnection) {
        var networkAddress = new NetworkHostAndPort(cordaRpcClientConnection.getHost(), cordaRpcClientConnection.getPort());
        var client = new CordaRPCClient(networkAddress);
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
        var gatewayParty = getParty(GATEWAY_PARTY_NAME);
        return proxy.startFlowDynamic(PaymentFlow.class, paymentInstruction, gatewayParty)
            .getReturnValue()
            .toCompletableFuture()
            .thenApply(it -> (Void) null)
            .minimalCompletionStage();
    }

    public CompletionStage<ParticipantRegistration> executeRegistrationFlow() {
        var gatewayParty = getParty(GATEWAY_PARTY_NAME);
        return proxy.startFlowDynamic(RegisterParticipantFlow.class, gatewayParty)
            .getReturnValue()
            .toCompletableFuture()
            .minimalCompletionStage();
    }

    public List<Payment> getPayments() {
        return proxy.vaultQuery(Payment.class)
            .getStates()
            .stream()
            .map(it -> it.getState().getData())
            .collect(Collectors.toUnmodifiableList());
    }

    public byte[] getAttachedPaymentInstruction(@NonNull SecureHash attachmentId) {
        try (
            var inputStream = proxy.openAttachment(attachmentId);
            var zipInputStream = new ZipInputStream(inputStream);
            var outputStream = new ByteArrayOutputStream()
        ) {
            for (;;) {
                var entry = zipInputStream.getNextEntry();
                if (entry == null) {
                    break;
                }
                if (entry.isDirectory() || !PAYMENT_INSTRUCTION_ATTACHMENT.equals(entry.getName())) {
                    continue;
                }
                var buffer = new byte[1024];
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
