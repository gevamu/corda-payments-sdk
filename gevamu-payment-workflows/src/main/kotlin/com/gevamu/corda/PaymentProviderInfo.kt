package com.gevamu.corda

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.util.UUID

@CordaSerializable
data class PaymentProviderInfo(val gatewayNode: Party, val providerId: UUID)
