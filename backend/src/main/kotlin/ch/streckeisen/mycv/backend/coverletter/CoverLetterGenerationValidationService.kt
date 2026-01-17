package ch.streckeisen.mycv.backend.coverletter

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import org.springframework.stereotype.Service

private const val LANGUAGE_FIELD = "language"
private const val STYLE_FIELD = "style"
private const val APPLICATION_FIELD = "application"
private const val JOB_TITLE_FIELD = "$APPLICATION_FIELD.jobTitle"
private const val COMPANY_FIELD = "$APPLICATION_FIELD.company"
private const val CONTACT_PERSON_FIELD = "$APPLICATION_FIELD.contactPerson"
private const val CONTACT_PERSON_FIRST_NAME_FIELD = "$APPLICATION_FIELD.contactPerson.firstName"
private const val CONTACT_PERSON_LAST_NAME_FIELD = "$APPLICATION_FIELD.contactPerson.lastName"
private const val ADDRESSEE_FIELD = "$APPLICATION_FIELD.addressee"
private const val SALUTATION_FIELD = "$APPLICATION_FIELD.salutation"
private const val CONTENT_FIELD = "$APPLICATION_FIELD.coverLetterContent"
private const val CLOSING_FIELD = "$APPLICATION_FIELD.closing"
private const val COMPANY_ADDRESS_FIELD = "$APPLICATION_FIELD.companyAddress"
private const val COMPANY_STREET_FIELD = "$COMPANY_ADDRESS_FIELD.street"
private const val COMPANY_ZIP_FIELD = "$COMPANY_ADDRESS_FIELD.postcode"
private const val COMPANY_CITY_FIELD = "$COMPANY_ADDRESS_FIELD.city"
private const val DOCUMENTS_FIELD = "attachedDocuments"

private const val CL_VALIDATION_PREFIX_KEY = "$MYCV_KEY_PREFIX.coverletter.validation"
private const val INVALID_LANGUAGE_MESSAGE_KEY = "$CL_VALIDATION_PREFIX_KEY.invalidLanguage"
private const val INVALID_STYLE_MESSAGE_KEY = "$CL_VALIDATION_PREFIX_KEY.invalidStyle"
private const val MISSING_APPLICATION_MESSAGE_KEY = "$CL_VALIDATION_PREFIX_KEY.applicationMissing"
private const val MISSING_ADDRESSEE_CONTACT_MESSAGE_KEY = "$CL_VALIDATION_PREFIX_KEY.addresseeOrContactMissing"
private const val MISSING_COMPANY_ADDRESS_MESSAGE_KEY = "$CL_VALIDATION_PREFIX_KEY.companyAddressMissing"
private const val INVALID_DOCUMENTS_MESSAGE_KEY = "$CL_VALIDATION_PREFIX_KEY.invalidDocuments"
private const val INVALID_REQUEST_MESSAGE_KEY = "$CL_VALIDATION_PREFIX_KEY.error"

@Service
class CoverLetterGenerationValidationService(
    private val stringValidator: StringValidator,
    private val messagesService: MessagesService
) {
    fun validateGenerationRequest(request: CoverLetterGenerationRequestDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        stringValidator.validateRequiredString(
            requiredField = LANGUAGE_FIELD,
            value = request.language,
            validationErrorBuilder = validationErrorBuilder
        )

        if (!messagesService.getSupportedLanguages().contains(request.language)) {
            val error = messagesService.getMessage(INVALID_LANGUAGE_MESSAGE_KEY)
            validationErrorBuilder.addError(LANGUAGE_FIELD, error)
        }

        if (CoverLetterStyle.fromStyleKey(request.style) == null) {
            val error = messagesService.getMessage(INVALID_STYLE_MESSAGE_KEY)
            validationErrorBuilder.addError(STYLE_FIELD, error)
        }

        if (request.application == null) {
            val error = messagesService.getMessage(MISSING_APPLICATION_MESSAGE_KEY)
            validationErrorBuilder.addError(APPLICATION_FIELD, error)
        } else {
            validateApplication(request.application, validationErrorBuilder)
        }

        if (request.attachedDocuments != null && request.attachedDocuments.any { it.isNullOrBlank() }) {
            val error = messagesService.getMessage(INVALID_DOCUMENTS_MESSAGE_KEY)
            validationErrorBuilder.addError(DOCUMENTS_FIELD, error)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(INVALID_REQUEST_MESSAGE_KEY)))
        }

        return Result.success(Unit)
    }

    fun validateApplication(
        application: CoverLetterApplicationDto,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        stringValidator.validateRequiredString(
            requiredField = JOB_TITLE_FIELD,
            value = application.jobTitle,
            validationErrorBuilder = validationErrorBuilder
        )

        stringValidator.validateRequiredString(
            requiredField = COMPANY_FIELD,
            value = application.company,
            validationErrorBuilder = validationErrorBuilder
        )

        if (application.contactPerson != null) {
            stringValidator.validateRequiredString(
                requiredField = CONTACT_PERSON_FIRST_NAME_FIELD,
                value = application.contactPerson.firstName,
                validationErrorBuilder = validationErrorBuilder
            )

            stringValidator.validateRequiredString(
                requiredField = CONTACT_PERSON_LAST_NAME_FIELD,
                value = application.contactPerson.lastName,
                validationErrorBuilder = validationErrorBuilder
            )
        }

        stringValidator.validateOptionalString(
            optionalField = ADDRESSEE_FIELD,
            value = application.addressee,
            validationErrorBuilder = validationErrorBuilder
        )

        if (application.addressee.isNullOrBlank() && application.contactPerson == null) {
            val error = messagesService.getMessage(MISSING_ADDRESSEE_CONTACT_MESSAGE_KEY)
            validationErrorBuilder.addError(CONTACT_PERSON_FIELD, error)
            validationErrorBuilder.addError(ADDRESSEE_FIELD, error)
        }

        stringValidator.validateRequiredString(
            requiredField = SALUTATION_FIELD,
            value = application.salutation,
            validationErrorBuilder = validationErrorBuilder
        )

        stringValidator.validateRequiredString(
            requiredField = CONTENT_FIELD,
            value = application.coverLetterContent,
            validationErrorBuilder = validationErrorBuilder
        )

        stringValidator.validateRequiredString(
            requiredField = CLOSING_FIELD,
            value = application.closing,
            validationErrorBuilder = validationErrorBuilder
        )

        if (application.companyAddress == null) {
            val error = messagesService.getMessage(MISSING_COMPANY_ADDRESS_MESSAGE_KEY)
            validationErrorBuilder.addError(COMPANY_ADDRESS_FIELD, error)
        } else {
            stringValidator.validateRequiredString(
                requiredField = COMPANY_STREET_FIELD,
                value = application.companyAddress.street,
                validationErrorBuilder = validationErrorBuilder
            )

            stringValidator.validateRequiredString(
                requiredField = COMPANY_ZIP_FIELD,
                value = application.companyAddress.postcode,
                validationErrorBuilder = validationErrorBuilder
            )

            stringValidator.validateRequiredString(
                requiredField = COMPANY_CITY_FIELD,
                value = application.companyAddress.city,
                validationErrorBuilder = validationErrorBuilder
            )
        }
    }
}