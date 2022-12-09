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

package com.gevamu.corda.test.contracts

import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import java.util.UUID

abstract class AbstractPaymentContractTest {
    companion object {
        fun createIdentity(organisation: String) = TestIdentity(CordaX500Name(organisation, "London", "GB"))
    }

    protected val payer = createIdentity("Payer")
    protected val gateway = createIdentity("Gateway")
    protected val ledgerServices = MockServices(
        listOf("com.gevamu.corda.contracts"),
        payer,
        gateway
    )

    protected val uniquePaymentId: UUID = UUID.randomUUID()
    protected val attachmentId = SecureHash.SHA256(ByteArray(32))
    protected val endToEndId = "a1b2c3d4e5f6"
}
