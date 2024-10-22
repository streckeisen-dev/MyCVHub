package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.EntityNotFoundException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
        return applicantAccountValidationService.validateSignupRequest(signupRequest)
            .fold(
                onSuccess = {
                    val encodedPassword = passwordEncoder.encode(signupRequest.password)
                    if (encodedPassword.length > PASSWORD_MAX_LENGTH) {
                        val errors = ValidationException.ValidationErrorBuilder()
                        errors.addError("password", messagesService.getMessage(ENCODED_PASSWORD_LENGTH_ERROR_KEY))
                        Result.failure(errors.build(messagesService.getMessage(PASSWORD_ENCODING_ERROR_KEY)))
                    } else {
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
                        Result.success(applicantAccountRepository.save(applicantAccount))
                    }
                },
                onFailure = {
                    Result.failure(it)
                }
            )
    }
}