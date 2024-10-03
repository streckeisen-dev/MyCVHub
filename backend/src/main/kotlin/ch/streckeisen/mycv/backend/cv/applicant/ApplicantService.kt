package ch.streckeisen.mycv.backend.cv.applicant

import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.privacy.PrivacySettingsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplicantService(
    private val applicantRepository: ApplicantRepository,
    private val applicantValidationService: ApplicantValidationService,
    private val passwordEncoder: PasswordEncoder,
    private val privacySettingsService: PrivacySettingsService
) {
    @Transactional(readOnly = true)
    fun findById(id: Long): Result<Applicant> {
        return applicantRepository.findById(id)
            .map { applicant -> Result.success(applicant) }
            .orElse(Result.failure(ResultNotFoundException("No applicant with ID $id")))
    }

    @Transactional(readOnly = false)
    fun create(signupRequest: SignupRequestDto): Result<Applicant> {
        return applicantValidationService.validateSignupRequest(signupRequest)
            .fold(
                onSuccess = {
                    val encodedPassword = passwordEncoder.encode(signupRequest.password)
                    if (encodedPassword.length > PASSWORD_MAX_LENGTH) {
                        val errors = ValidationException.ValidationErrorBuilder()
                        errors.addError("password", "Encoded password exceeds number of allowed characters")
                        Result.failure(errors.build("Password encoding failed"))
                    } else {
                        val applicant = Applicant(
                            signupRequest.firstName!!,
                            signupRequest.lastName!!,
                            null,
                            signupRequest.email!!,
                            signupRequest.phone!!,
                            signupRequest.birthday!!,
                            signupRequest.street!!,
                            signupRequest.houseNumber!!,
                            signupRequest.postcode!!,
                            signupRequest.city!!,
                            signupRequest.country!!,
                            encodedPassword,
                            privacySettings = privacySettingsService.getDefaultSettings(signupRequest.hasPublicProfile)
                        )
                        Result.success(applicantRepository.save(applicant))
                    }
                },
                onFailure = {
                    Result.failure(it)
                }
            )
    }
}