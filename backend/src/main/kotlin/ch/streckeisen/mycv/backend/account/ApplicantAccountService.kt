package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.EntityNotFoundException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

private const val ENCODED_PASSWORD_LENGTH_ERROR_KEY = "${MYCV_KEY_PREFIX}.account.validation.password.encodingTooLong"
private const val PASSWORD_ENCODING_ERROR_KEY = "${MYCV_KEY_PREFIX}.account.validation.password.encodingError"

@Service
class ApplicantAccountService(
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val applicantAccountValidationService: ApplicantAccountValidationService,
    private val passwordEncoder: PasswordEncoder,
    private val messagesService: MessagesService
) {
    @Transactional(readOnly = true)
    fun findById(id: Long): Result<ApplicantAccountEntity> {
        return applicantAccountRepository.findById(id)
            .map { applicant -> Result.success(applicant) }
            .orElse(Result.failure(EntityNotFoundException("No applicant with ID $id")))
    }

    @Transactional(readOnly = false)
    fun create(signupRequest: SignupRequestDto): Result<ApplicantAccountEntity> {
        applicantAccountValidationService.validateSignupRequest(signupRequest)
            .onFailure { return Result.failure(it) }
        val encodedPassword = encodePassword(signupRequest.password)
            .getOrElse { return Result.failure(it) }

        val applicantAccount = ApplicantAccountEntity(
            signupRequest.firstName!!,
            signupRequest.lastName!!,
            signupRequest.email!!,
            signupRequest.phone!!,
            signupRequest.birthday!!,
            signupRequest.street!!,
            signupRequest.houseNumber,
            signupRequest.postcode!!,
            signupRequest.city!!,
            signupRequest.country!!,
            encodedPassword
        )
        return Result.success(applicantAccountRepository.save(applicantAccount))
    }

    @Transactional
    fun update(accountId: Long, accountUpdate: AccountUpdateDto): Result<ApplicantAccountEntity> {
        val existingAccount = applicantAccountRepository.findById(accountId)
            .getOrElse { return Result.failure(EntityNotFoundException("Account does not exist")) }

        applicantAccountValidationService.validateAccountUpdate(accountId, accountUpdate)
            .onFailure { return Result.failure(it) }

        val account = ApplicantAccountEntity(
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
            existingAccount.password,
            existingAccount.id,
            existingAccount.profile
        )
        return Result.success(applicantAccountRepository.save(account))
    }

    @Transactional
    fun changePassword(accountId: Long, changePasswordDto: ChangePasswordDto): Result<ApplicantAccountEntity> {
        val existingAccount = applicantAccountRepository.findById(accountId)
            .getOrElse { return Result.failure(EntityNotFoundException("Account does not exist")) }

        applicantAccountValidationService.validateChangePasswordRequest(changePasswordDto, existingAccount.password)
            .onFailure { return Result.failure(it) }

        val encodedNewPassword = encodePassword(changePasswordDto.password)
            .getOrElse { return Result.failure(it) }

        val account = ApplicantAccountEntity(
            existingAccount.firstName,
            existingAccount.lastName,
            existingAccount.email,
            existingAccount.phone,
            existingAccount.birthday,
            existingAccount.street,
            existingAccount.houseNumber,
            existingAccount.postcode,
            existingAccount.city,
            existingAccount.country,
            encodedNewPassword,
            existingAccount.id,
            existingAccount.profile
        )

        return Result.success(applicantAccountRepository.save(account))
    }

    private fun encodePassword(password: String?): Result<String> {
        val encodedPassword = passwordEncoder.encode(password)
        if (encodedPassword.length > PASSWORD_MAX_LENGTH) {
            val errors = ValidationException.ValidationErrorBuilder()
            errors.addError("password", messagesService.getMessage(ENCODED_PASSWORD_LENGTH_ERROR_KEY))
            return Result.failure(errors.build(messagesService.getMessage(PASSWORD_ENCODING_ERROR_KEY)))
        }
        return Result.success(encodedPassword)
    }
}