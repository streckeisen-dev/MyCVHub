package ch.streckeisen.mycv.backend.admin.bootstrap

import org.springframework.stereotype.Component
import java.security.SecureRandom

private const val TEMPORARY_PASSWORD_LENGTH = 24
private const val TEMPORARY_PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789-_"

@Component
class AdminTemporaryPasswordGenerator {
    private val secureRandom = SecureRandom()

    fun generate(): String {
        return (1..TEMPORARY_PASSWORD_LENGTH)
            .map { TEMPORARY_PASSWORD_ALPHABET[secureRandom.nextInt(TEMPORARY_PASSWORD_ALPHABET.length)] }
            .joinToString("")
    }
}
