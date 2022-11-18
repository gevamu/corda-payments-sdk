package com.gevamu.simulator.flows

import co.paralleluniverse.fibers.Suspendable
import com.gevamu.contracts.PaymentContract
import com.gevamu.states.Payment
import java.time.Instant
import java.util.UUID
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@Deprecated("Temporary simulator, to be removed and replaced with POST listener")
@InitiatingFlow
@StartableByRPC
class ApproveOrRejectPaymentFlow(
    private val paymentId: UUID,
    private val newPaymentStatus: Payment.PaymentStatus,
) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        // Get payment from the vault
        val inputCriteria: QueryCriteria.LinearStateQueryCriteria = QueryCriteria.LinearStateQueryCriteria()
            .withUuid(listOf(paymentId))
            // Shouldn't be archived
            .withStatus(Vault.StateStatus.UNCONSUMED)
            /*// Payer should be a participant
            .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT)*/
        val paymentStateAndRef = serviceHub.vaultService.queryBy(Payment::class.java, inputCriteria).states.first()

        // Mutate it to change status and timestamp
        val payment = paymentStateAndRef.state.data.copy(
            status = newPaymentStatus,
            timestamp = Instant.now(),
        )

        // Prepare transaction to send over the state
        // TODO It's better to replace notary with named one since we are consuming states
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val payer = paymentStateAndRef.state.data.payer
        val builder = TransactionBuilder(notary)
            .addInputState(paymentStateAndRef)
            .addOutputState(payment)
            .addCommand(PaymentContract.Commands.UpdateStatus(), ourIdentity.owningKey, payer.owningKey)
        builder.verify(serviceHub)

        // Sign the transaction on our payer cordapp
        val partiallySignedTransaction = serviceHub.signInitialTransaction(builder)

        // Notify gateway about incoming payment tx
        val payerSession: FlowSession = initiateFlow(payer)

        // Expect gateway to sign the transaction
        val fullySignedTransaction = subFlow(CollectSignaturesFlow(partiallySignedTransaction, listOf(payerSession)))

        // Record signed transaction to the vault
        subFlow(FinalityFlow(fullySignedTransaction, listOf(payerSession)))
    }
}


@InitiatedBy(ApproveOrRejectPaymentFlow::class)
class ApproveAndDenyPaymentFlowResponder(private val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
//        val registrationService: RegistrationService = serviceHub.cordaService(RegistrationService::class.java)

//        val xmlService: GatewayXmlService = serviceHub.cordaService(GatewayXmlService::class.java)
//        val webService: WebService = serviceHub.cordaService(WebService::class.java)

        // Check and confirm transaction
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) {
                // TODO Unmarshall attachment; check that state fields equal to payment instruction fields;
                //      when signing is implemented, check signature

//                val payment = stx.tx.outputStates.first() as Payment
//                val paymentInstruction = xmlService.loadPaymentInstruction(payment.paymentInstructionId)
//
//                val participantId = paymentInstruction.pmtInf.firstOrNull()?.dbtr?.id?.orgId?.othr?.firstOrNull()?.id
//                // TODO Don't use check
//                check(
//                    participantId != null &&
//                        registrationService.isParticipant(participantId, counterpartySession.counterparty)
//                ) {
//                    "To send payment flow payer must be a participant"
//                }
            }
        }

        // Sign transaction
        val signedTransaction = subFlow(signTransactionFlow)

//        val payment = signedTransaction.tx.outputStates.first() as Payment
//        val paymentInstruction = xmlService.loadPaymentInstruction(payment.paymentInstructionId)
//        webService.sendPaymentInstruction(payment.linearId, paymentInstruction)

        // Confirm completion of the flow on our side
        subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = signedTransaction.id))
    }
}
