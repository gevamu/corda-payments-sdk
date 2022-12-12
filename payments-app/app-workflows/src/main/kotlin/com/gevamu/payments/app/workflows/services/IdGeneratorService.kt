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
