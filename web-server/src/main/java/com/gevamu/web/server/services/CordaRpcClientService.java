package com.gevamu.web.server.services;

import com.gevamu.flows.ParticipantRegistration;
import com.gevamu.flows.PaymentFlow;
import com.gevamu.flows.PaymentInstruction;
import com.gevamu.payments.app.contracts.schemas.AppSchemaV1;
import com.gevamu.payments.app.contracts.states.PaymentDetails;
import com.gevamu.payments.app.contracts.states.PaymentDetailsState;
import com.gevamu.payments.app.workflows.flows.CreditorRetrievalFlow;
import com.gevamu.payments.app.workflows.flows.DebtorRetrievalFlow;
import com.gevamu.payments.app.workflows.flows.PaymentInitiationFlow;
import com.gevamu.payments.app.workflows.flows.PaymentInitiationRequest;
import com.gevamu.payments.app.workflows.flows.RegistrationInitiationFlow;
import com.gevamu.payments.app.workflows.flows.RegistrationRetrievalFlow;
import com.gevamu.states.Payment;
import com.gevamu.web.server.config.CordaRpcClientConnection;
import com.gevamu.web.server.models.ParticipantAccount;
import com.gevamu.web.server.models.PaymentState;
import com.gevamu.web.server.util.MoreCollectors;
import lombok.NonNull;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.CordaRuntimeException;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
            .thenApply(it -> (Void) null);
    }

    public CompletionStage<List<? extends AppSchemaV1.Account>> getCreditors() {
        return executeFlow(CreditorRetrievalFlow.class);
    }

    public CompletionStage<List<? extends AppSchemaV1.Account>> getDebtors() {
        return executeFlow(DebtorRetrievalFlow.class);
    }

    public CompletionStage<Void> sendPayment(PaymentInitiationRequest request) {
        return executeFlow(PaymentInitiationFlow.class, request)
            .thenApply(it -> null);
    }

    public CompletionStage<ParticipantRegistration> getRegistration() {
        return executeFlow(RegistrationRetrievalFlow.class);
    }

    public CompletionStage<ParticipantRegistration> register() {
        return executeFlow(RegistrationInitiationFlow.class);
    }

    public <O> CompletionStage<O> executeFlow(@NonNull Class<? extends FlowLogic<O>> flowClass, Object... args) {
        Party gatewayParty = getParty(GATEWAY_PARTY_NAME);
        Object[] flowArgs = ArrayUtils.isEmpty(args) ?
            new Object[] { gatewayParty } :
            ArrayUtils.add(args, gatewayParty);
        return proxy.startFlowDynamic(flowClass, flowArgs)
            .getReturnValue()
            .toCompletableFuture();
    }

    public List<PaymentState> getPaymentDetails() {
        return proxy.vaultQuery(PaymentDetailsState.class)
            .getStates()
            .stream()
            .map(it -> {
                UUID paymentId = it.getState().getData().getId();
                PaymentDetails paymentDetails = it.getState().getData().getPaymentDetails();
                return PaymentState.builder()
                    .paymentId(paymentId)
                    .endToEndId(paymentDetails.getEndToEndId())
                    .debtor(ParticipantAccount.fromDebtor(paymentDetails.getDebtor()))
                    .creditor(ParticipantAccount.fromCreditor(paymentDetails.getCreditor()))
                    .currency(paymentDetails.getCurrency().getIsoCode())
                    .amount(paymentDetails.getAmount())
                    .creationTime(paymentDetails.getCreationTime())
                    .status(Payment.PaymentStatus.CREATED)//fixme
                    .updateTime(paymentDetails.getCreationTime())//fixme
                    .build();
            })
            .collect(MoreCollectors.toUnmodifiableList());
    }

    public List<Payment> getPaymentStatus(UUID paymentId) {
        /*CriteriaExpression expression = CriteriaExpression.ColumnPredicateExpression("uniquePaymentId", UUID.class);
        QueryCriteria queryCriteria = QueryCriteria.VaultCustomQueryCriteria(
            PaymentSchemaV1.PersistentPayment.getUniquePaymentId().equal(paymentId)
        );*/
        return proxy.vaultQuery(Payment.class)
            .getStates()
            .stream()
            .map(it -> it.getState().getData())
            .collect(MoreCollectors.toUnmodifiableList());
    }

    @Deprecated
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
