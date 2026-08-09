package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountDto
import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.account.dto.AuthResponseDto
import ch.streckeisen.mycv.backend.account.verification.AccountVerificationService
import ch.streckeisen.mycv.backend.coverletter.CoverLetterGenerationSnapshot
import ch.streckeisen.mycv.backend.cv.profile.ThumbnailDto
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.security.MyCvPrincipal
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

private val logger = KotlinLogging.logger {}

@Service
class ApplicantAccountService(
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val applicantAccountValidationService: ApplicantAccountValidationService,
    private val accountVerificationService: AccountVerificationService,
    private val profilePictureService: ProfilePictureService
) {
    @Transactional(readOnly = true)
    fun findById(id: Long): Result<AccountDto> {
        return findEntityById(id)
            .map { account -> account.toAccountDto() }
    }

    @Transactional(readOnly = true)
    fun getAuthResponse(principal: MyCvPrincipal): Result<AuthResponseDto> {
        val account = applicantAccountRepository.findById(principal.id)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }
        val accountDetails = account.accountDetails
        val profile = account.profile
        val displayName = if (accountDetails != null) {
            accountDetails.firstName + " " + accountDetails.lastName
        } else null
        val hasProfile = profile != null
        val thumbnail = if (hasProfile) {
            profilePictureService.getThumbnail(account.id!!, profile).getOrNull()
        } else null

        return Result.success(
            AuthResponseDto(
                username = principal.username,
                authLevel = principal.status,
                displayName = displayName,
                language = accountDetails?.language,
                hasProfile = hasProfile,
                thumbnail = thumbnail?.let { ThumbnailDto(it.uri.toString()) }
            )
        )
    }

    @Transactional(readOnly = true)
    fun findEntityById(id: Long): Result<ApplicantAccountEntity> {
        val account = applicantAccountRepository.findById(id)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }

        return Result.success(account)
    }

    @Transactional(readOnly = true)
    fun findByIdForCoverLetterGeneration(id: Long): Result<CoverLetterGenerationSnapshot> {
        val account = applicantAccountRepository.findById(id)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }
        val details = account.accountDetails
        val profile = account.profile

        return Result.success(
            CoverLetterGenerationSnapshot(
                accountId = account.id!!,
                isVerified = account.isVerified,
                firstName = details?.firstName,
                lastName = details?.lastName,
                email = details?.email,
                phone = details?.phone,
                street = details?.street,
                houseNumber = details?.houseNumber,
                postcode = details?.postcode,
                city = details?.city,
                profilePicture = profile?.profilePicture,
                jobTitle = profile?.jobTitle
            )
        )
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
    fun update(accountId: Long, accountUpdate: AccountUpdateDto): Result<AccountDto> {
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
                accountUpdate.country!!,
                accountUpdate.language!!
            ),
            id = existingAccount.id,
            profile = existingAccount.profile,
            oauthIntegrations = existingAccount.oauthIntegrations,
            accountVerification = existingAccount.accountVerification,
            applications = existingAccount.applications,
            applicationTemplates = existingAccount.applicationTemplates
        )
        val result = applicantAccountRepository.save(account)
        if (!account.isVerified) {
            accountVerificationService.generateVerificationToken(accountId)
                .onFailure { logger.error(it) { "[Account ${accountId}] Failed to generate new verification token for new email address" } }
        }
        return Result.success(result.toAccountDto())
    }

    @Transactional
    fun delete(accountId: Long): Result<Unit> {
        val existingAccount = applicantAccountRepository.findById(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }

        applicantAccountRepository.delete(existingAccount)
        return Result.success(Unit)
    }
}
