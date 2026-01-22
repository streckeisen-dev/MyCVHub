package ch.streckeisen.mycv.backend.coverletter

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import ch.streckeisen.mycv.backend.util.assertValidationResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val COMPANY_NAME = "My Company"

class CoverLetterGenerationValidationServiceTest {
    private lateinit var coverLetterGenerationValidationService: CoverLetterGenerationValidationService

    @BeforeEach
    fun setup() {
        val messagesService = mockk<MessagesService>(relaxed = true) {
            every { getSupportedLanguages() } returns listOf("de", "en")
        }
        val stringValidator = StringValidator(messagesService)
        coverLetterGenerationValidationService =
            CoverLetterGenerationValidationService(stringValidator, messagesService)
    }

    @Test
    fun testValidateLanguageWithNullValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateLanguage(null, validationErrorBuilder)

        assertEquals(1, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateLanguageWithBlankValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateLanguage("", validationErrorBuilder)

        assertEquals(1, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateLanguageWithInvalidValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateLanguage("invalid", validationErrorBuilder)

        assertEquals(1, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateLanguageWithValidValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateLanguage("de", validationErrorBuilder)

        assertEquals(0, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateCoverLetterStyleWithInvalidValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateCoverLetterStyle("invalid", validationErrorBuilder)

        assertEquals(1, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateCoverLetterStyleWithValidValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateCoverLetterStyle(
            CoverLetterStyle.MODERN.styleKey,
            validationErrorBuilder
        )

        assertEquals(0, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateDocumentsWithNullValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateDocuments(null, validationErrorBuilder)

        assertEquals(0, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateDocumentsWithEmptyList() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateDocuments(emptyList(), validationErrorBuilder)

        assertEquals(1, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateDocumentsWithNullValueInList() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateDocuments(listOf(null), validationErrorBuilder)

        assertEquals(1, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateDocumentsWithValidValueInList() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateDocuments(listOf("valid"), validationErrorBuilder)

        assertEquals(0, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateContentWithNullValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateContent(null, validationErrorBuilder)

        assertEquals(1, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateContentWithValidValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateContent("valid", validationErrorBuilder)

        assertEquals(0, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateClosingWithBlankValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateClosing("", validationErrorBuilder)

        assertEquals(1, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateClosingWithValidValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateClosing("valid", validationErrorBuilder)

        assertEquals(0, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithInvalidValue() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = null,
                addressee = null,
                salutation = null,
                companyAddress = null,
                content = null,
                closing = null
            ), validationErrorBuilder
        )

        assertEquals(8, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidJobTitle() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = "Developer",
                company = null,
                contactPerson = null,
                addressee = null,
                salutation = null,
                companyAddress = null,
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(7, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidCompany() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = COMPANY_NAME,
                contactPerson = null,
                addressee = null,
                salutation = null,
                companyAddress = null,
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(7, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithIncompleteContactPerson() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = CoverLetterContactPersonDto(null, null),
                addressee = null,
                salutation = null,
                companyAddress = null,
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(8, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidContactPersonFirstName() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = CoverLetterContactPersonDto("first", null),
                addressee = null,
                salutation = null,
                companyAddress = null,
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(7, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidContactPersonLastName() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = CoverLetterContactPersonDto(null, "last"),
                addressee = null,
                salutation = null,
                companyAddress = null,
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(7, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidContactPerson() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = CoverLetterContactPersonDto("first", "last"),
                addressee = null,
                salutation = null,
                companyAddress = null,
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(6, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidAddressee() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = null,
                addressee = "a",
                salutation = null,
                companyAddress = null,
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(6, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidSalutation() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = null,
                addressee = null,
                salutation = "hi",
                companyAddress = null,
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(7, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithIncompleteCompanyAddress() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = null,
                addressee = null,
                salutation = null,
                companyAddress = CoverLetterCompanyAddressDto(null, null, null),
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(10, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidCompanyAddressStreet() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = null,
                addressee = null,
                salutation = null,
                companyAddress = CoverLetterCompanyAddressDto("street", null, null),
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(9, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidCompanyAddressPostcode() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = null,
                addressee = null,
                salutation = null,
                companyAddress = CoverLetterCompanyAddressDto(null, "valid", null),
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(9, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidCompanyAddressCity() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = null,
                addressee = null,
                salutation = null,
                companyAddress = CoverLetterCompanyAddressDto(null, null, "c"),
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(9, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidCompanyAddress() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = null,
                addressee = null,
                salutation = null,
                companyAddress = CoverLetterCompanyAddressDto("street", "1234", "c"),
                content = null,
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(7, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidContent() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = null,
                addressee = null,
                salutation = null,
                companyAddress = null,
                content = "valid",
                closing = null
            ),
            validationErrorBuilder
        )

        assertEquals(7, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidClosing() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = null,
                company = null,
                contactPerson = null,
                addressee = null,
                salutation = null,
                companyAddress = null,
                content = null,
                closing = "close"
            ),
            validationErrorBuilder
        )

        assertEquals(7, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateApplicationWithValidApplication() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        coverLetterGenerationValidationService.validateApplication(
            CoverLetterApplicationDto(
                jobTitle = "Developer",
                company = COMPANY_NAME,
                contactPerson = CoverLetterContactPersonDto("first", "last"),
                addressee = null,
                salutation = "abc",
                companyAddress = CoverLetterCompanyAddressDto("street", "1234", "c"),
                content = "content",
                closing = "close"
            ),
            validationErrorBuilder
        )

        assertEquals(0, validationErrorBuilder.errors().size)
    }

    @Test
    fun testValidateGenerationRequestWithMissingApplication() {
        val result = coverLetterGenerationValidationService.validateGenerationRequest(
            CoverLetterGenerationRequestDto(
                language = "de",
                style = CoverLetterStyle.MODERN.styleKey,
                mirrorProfileImage = true,
                documents = null,
                application = null,
            )
        )

        assertValidationResult(result, false, 1)
    }

    @Test
    fun testValidateGenerationRequestWithMissingMirrorProfileImage() {
        val result = coverLetterGenerationValidationService.validateGenerationRequest(
            CoverLetterGenerationRequestDto(
                language = "de",
                style = CoverLetterStyle.MODERN.styleKey,
                mirrorProfileImage = null,
                documents = null,
                application = CoverLetterApplicationDto(
                    jobTitle = "Developer",
                    company = COMPANY_NAME,
                    contactPerson = CoverLetterContactPersonDto("first", "last"),
                    addressee = null,
                    salutation = "abc",
                    companyAddress = CoverLetterCompanyAddressDto("street", "1234", "c"),
                    content = "content",
                    closing = "close"
                )
            )
        )

        assertValidationResult(result, true, 0)
    }
}