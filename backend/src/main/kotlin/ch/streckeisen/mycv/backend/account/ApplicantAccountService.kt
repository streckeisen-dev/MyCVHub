package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplicantAccountService(
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val applicantAccountValidationService: ApplicantAccountValidationService,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional(readOnly = true)
    fun findById(id: Long): Result<ApplicantAccountEntity> {
        return applicantAccountRepository.findById(id)
            .map { applicant -> Result.success(applicant) }
            .orElse(Result.failure(ResultNotFoundException("No applicant with ID $id")))
    }

    @Transactional(readOnly = false)
    fun create(signupRequest: SignupRequestDto): Result<ApplicantAccountEntity> {
        return applicantAccountValidationService.validateSignupRequest(signupRequest)
            .fold(
                onSuccess = {
                    val encodedPassword = passwordEncoder.encode(signupRequest.password)
                    if (encodedPassword.length > PASSWORD_MAX_LENGTH) {
                        val errors = ValidationException.ValidationErrorBuilder()
                        errors.addError("password", "Encoded password exceeds number of allowed characters")
                        Result.failure(errors.build("Password encoding failed"))
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