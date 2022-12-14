# corda-payments-sdk

## Installation

TODO: publish package and describe

## Usage

### Create service class

```java
// Model for RegisterParticipantFlow result
import com.gevamu.flows.ParticipantRegistration;
// Flow for participant registration
import com.gevamu.flows.RegisterParticipantFlow;

// Flow payment execution
import com.gevamu.flows.PaymentFlow;
// Model for PaymentFlow input
import com.gevamu.flows.PaymentInstruction;
// Model of payment
import com.gevamu.states.Payment;
```

```java
class PaymentService{
    private static final String GATEWAY_PARTY_NAME = "Gateway";
    private static final String PAYMENT_INSTRUCTION_ATTACHMENT = "paymentInstruction.xml";

    private final transient CordaRPCConnection connection;
    private final transient CordaRPCOps proxy;
    
    public setup(
            String host,    // Corda node host 
            Integer port,   // Corda node port 
            String user,    // RPC user 
            String password // RPC user password 
    ){
        NetworkHostAndPort networkAddress = new NetworkHostAndPort(host, port);
        CordaRPCClient client = new CordaRPCClient(networkAddress);
        connection = client.start(user, password);
        proxy = connection.getProxy();
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

    public List<StateAndRef<Payment>> getPayments() {
        return proxy.vaultQuery(Payment.class)
                .getStates();
    }
}
```

### Register corda node

```java
class Main{
    public static void main(){
        PaymentService service = new PaymentSerice();
        service.setup("localhost", 10012, "user", "password");
        
        service.executeRegistrationFlow();
    }
}
```

### Execute payment

```java
class Main{
    public static void main(){
        PaymentService service = new PaymentSerice();
        service.setup("localhost", 10012, "user", "password");
        
        PaymentInstruction paymentInstruction = new PaymentInstruction(
                PaymentInstructionFormat.ISO20022_V9_XML_UTF8,
                byteArray // Payment instruction in specified format cast to ByteArray 
        );
        service.executePaymentFlow(paymentInstruction);
    }
}
```

### Get list of payments

```java
class Main{
    public static void main(){
        PaymentService service = new PaymentSerice();
        service.setup("localhost", 10012, "user", "password");

        List<StateAndRef<Payment>> payments = service.getPayments();
    }
}
```

