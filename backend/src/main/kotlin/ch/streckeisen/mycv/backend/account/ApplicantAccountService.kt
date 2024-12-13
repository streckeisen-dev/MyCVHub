package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.account.verification.AccountVerificationService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

private val logger = KotlinLogging.logger {}

@Service
class ApplicantAccountService(
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val applicantAccountValidationService: ApplicantAccountValidationService,
    private val accountVerificationService: AccountVerificationService
) {
    @Transactional(readOnly = true)
    fun findById(id: Long): Result<ApplicantAccountEntity> {
        return applicantAccountRepository.findById(id)
            .map { applicant -> Result.success(applicant) }
            .orElse(Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")))
    }

    @Transactional(readOnly = true)
    fun getAccountStatus(accountId: Long): Result<AccountStatus> {
        val hasAccountDetails = applicantAccountRepository.hasAccountDetails(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }
        if (!hasAccountDetails) {
            return Result.success(AccountStatus.INCOMPLETE)
        }

        val isVerified = applicantAccountRepository.isAccountVerified(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }

        return if (isVerified) {
            Result.success(AccountStatus.VERIFIED)
        } else {
            Result.success(AccountStatus.UNVERIFIED)
        }
    }

    @Transactional
    fun update(accountId: Long, accountUpdate: AccountUpdateDto): Result<ApplicantAccountEntity> {
        val existingAccount = applicantAccountRepository.findById(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }

        applicantAccountValidationService.validateAccountUpdate(accountId, accountUpdate)
            .onFailure { return Result.failure(it) }

        val isVerified = if (existingAccount.accountDetails?.email == accountUpdate.email) {
            existingAccount.isVerified
        } else false

        val account = ApplicantAccountEntity(
            accountUpdate.username!!,
            existingAccount.password,
            existingAccount.isOAuthUser,
            isVerified,
            accountDetails = AccountDetailsEntity(
                accountUpdate.firstName!!,
                accountUpdate.lastName!!,
                accountUpdate.email!!,
                accountUpdate.phone!!,
                accountUpdate.birthday!!,
                accountUpdate.street!!,
                accountUpdate.houseNumber,
                accountUpdate.postcode!!,
                accountUpdate.city!!,
                accountUpdate.country!!
            ),
            id = existingAccount.id,
            profile = existingAccount.profile,
            oauthIntegrations = existingAccount.oauthIntegrations,
            accountVerification = existingAccount.accountVerification
        )
        applicantAccountRepository.save(account)
        if (!account.isVerified) {
            accountVerificationService.generateVerificationToken(accountId)
                .onFailure { logger.error(it) { "[Account ${accountId}] Failed to generate new verification token for new email address" } }
        }
        return Result.success(account)
    }

    @Transactional
    fun delete(accountId: Long): Result<Unit> {
        val existingAccount = applicantAccountRepository.findById(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }

        applicantAccountRepository.delete(existingAccount)
        return Result.success(Unit)
    }
}