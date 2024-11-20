package ch.streckeisen.mycv.backend.account.verification

import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.email.EmailService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

private val logger = KotlinLogging.logger { }

@Service
class AccountVerificationService(
    private val accountVerificationRepository: AccountVerificationRepository,
    private val emailService: EmailService,
    private val applicantAccountRepository: ApplicantAccountRepository,
    @Value("\${my-cv.account.verification.token-expiration-hours}")
    private val verificationTokenExpirationHours: Long,
    @Value("\${my-cv.account.verification.token-generation-block-minutes}")
    private val tokenGenerationBlockMinutes: Long
) {
    @Transactional
    fun generateVerificationToken(accountId: Long): Result<Unit> {
        val account = applicantAccountRepository.findById(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }

        if (account.isVerified) {
            logger.debug { "[Account ${accountId}] Account is already verified" }
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.verification.alreadyVerified"))
        }

        val existingVerificationToken = accountVerificationRepository.findByAccountId(accountId)
            .getOrNull()

        if (existingVerificationToken != null) {
            val creationTime = existingVerificationToken.expirationDate.minusHours(verificationTokenExpirationHours)
            if (LocalDateTime.now().isBefore(creationTime.plusMinutes(tokenGenerationBlockMinutes))) {
                logger.debug { "[Account ${accountId}] Verification token generation blocked" }
                return Result.failure(
                    LocalizedException(
                        "${MYCV_KEY_PREFIX}.account.verification.tokenGenerationBlocked",
                        tokenGenerationBlockMinutes.toString()
                    )
                )
            }
        }

        val accountVerificationEntity = AccountVerificationEntity(
            UUID.randomUUID().toString(),
            LocalDateTime.now().plusHours(verificationTokenExpirationHours),
            account,
            existingVerificationToken?.id
        )
        accountVerificationRepository.save(accountVerificationEntity)
        logger.debug { "[Account ${accountId}] Generated account verification token" }
        return emailService.sendAccountVerificationEmail(account, accountVerificationEntity.token)
    }

    @Transactional
    fun verifyToken(accountId: Long, token: String): Result<Unit> {
        val account = applicantAccountRepository.findById(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }

        val verificationEntity = accountVerificationRepository.findByAccountId(account.id!!)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.verification.noToken")) }

        if (LocalDateTime.now().isAfter(verificationEntity.expirationDate)) {
            logger.debug { "[Account ${accountId}] Verification token expired" }
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.verification.tokenExpired"))
        }

        if (verificationEntity.token != token) {
            logger.warn { "[Account ${accountId}] Verification token is invalid" }
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.verification.tokenInvalid"))
        }

        accountVerificationRepository.deleteById(verificationEntity.id!!)
        applicantAccountRepository.setAccountVerified(accountId)

        logger.debug { "[Account ${accountId}] Successfully verified account" }
        return Result.success(Unit)
    }
}