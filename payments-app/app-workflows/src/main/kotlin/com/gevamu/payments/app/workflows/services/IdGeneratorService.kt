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

package com.gevamu.payments.app.workflows.services

import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import org.apache.commons.lang3.RandomStringUtils
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale
import javax.xml.bind.DatatypeConverter

@CordaService
class IdGeneratorService(
    private val serviceHub: AppServiceHub
) : SingletonSerializeAsToken() {

    fun generateId(): String {
        return try {
            val str = RandomStringUtils.randomAlphanumeric(ID_LENGTH)
            val digest = MessageDigest.getInstance(ALGORITHM)
            digest.update(str.toByteArray())
            val bytes = digest.digest(str.toByteArray())
            DatatypeConverter.printHexBinary(bytes).toLowerCase(Locale.getDefault())
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    fun generateEndToEndId(): String {
        val id = generateId()
        return id.substring(0, END_TO_END_ID_LENGTH)
    }

    companion object {
        private const val ID_LENGTH = 32
        private const val END_TO_END_ID_LENGTH = 13
        private const val ALGORITHM = "MD5"
    }
}
