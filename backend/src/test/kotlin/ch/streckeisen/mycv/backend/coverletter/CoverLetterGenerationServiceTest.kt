package ch.streckeisen.mycv.backend.coverletter

import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.cv.generator.typst.TypstService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePicture
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import tools.jackson.databind.ObjectMapper
import java.io.File

private val invalidRequest = CoverLetterGenerationRequestDto(
    language = null,
    style = null,
    mirrorProfileImage = null,
    application = null,
    documents = null
)

private val validRequest = CoverLetterGenerationRequestDto(
    language = "de",
    style = CoverLetterStyle.MODERN.styleKey,
    mirrorProfileImage = false,
    application = CoverLetterApplicationDto(
        jobTitle = "Developer",
        company = "My Company",
        contactPerson = CoverLetterContactPersonDto("first", "last"),
        addressee = null,
        salutation = "abc",
        companyAddress = CoverLetterCompanyAddressDto("street", "1234", "c"),
        content = "content",
        closing = "close"
    ),
    documents = null
)

class CoverLetterGenerationServiceTest {
    private val profilePictureUri = this.javaClass.classLoader.getResource("profile.png")!!.toURI()

    private lateinit var coverLetterValidationService: CoverLetterGenerationValidationService
    private lateinit var applicantAccountService: ApplicantAccountService
    private lateinit var profilePictureService: ProfilePictureService
    private lateinit var objectMapper: ObjectMapper
    private lateinit var typstService: TypstService
    private lateinit var coverLetterGenerationService: CoverLetterGenerationService

    @BeforeEach
    fun setup() {
        coverLetterValidationService = mockk {
            every { validateGenerationRequest(eq(invalidRequest)) } returns Result.failure(IllegalArgumentException())
            every { validateGenerationRequest(eq(validRequest)) } returns Result.success(Unit)
        }

        applicantAccountService = mockk {
            every { findById(eq(1)) } returns Result.success(mockk {
                every { id } returns 1
                every { isVerified } returns true
                every { profile } returns mockk {
                    every { jobTitle } returns "Developer"
                }
                every { accountDetails } returns mockk {
                    every { firstName } returns "First"
                    every { lastName } returns "Last"
                    every { phone } returns "123456789"
                    every { email } returns "a@b.c"
                    every { street } returns "street"
                    every { houseNumber } returns "5"
                    every { postcode } returns "123"
                    every { city } returns "city"
                }
            })
            every { findById(eq(4)) } returns Result.success(mockk {
                every { id } returns 4
                every { profile } returns null
                every { accountDetails } returns null
                every { isVerified } returns false
            })
            every { findById(eq(5)) } returns Result.failure(IllegalArgumentException())
        }

        profilePictureService = mockk {

        }

        objectMapper = mockk {

        }

        typstService = mockk {

        }

        coverLetterGenerationService = CoverLetterGenerationService(
            coverLetterValidationService,
            applicantAccountService,
            profilePictureService,
            objectMapper,
            typstService
        )
    }

    @Test
    suspend fun testGenerateCoverLetterWithInvalidRequest() {
        val result = coverLetterGenerationService.generateCoverLetter(1, invalidRequest)

        assertTrue { result.isFailure }
        coVerify(exactly = 0) { typstService.compile(any(), any(), any()) }
    }

    @Test
    suspend fun testGenerateCoverLetterWithNotExistingAccount() {
        val result = coverLetterGenerationService.generateCoverLetter(5, validRequest)

        assertTrue { result.isFailure }
        coVerify(exactly = 0) { typstService.compile(any(), any(), any()) }
    }

    @Test
    suspend fun testGenerateCoverLetterWithIncompleteAccount() {
        val result = coverLetterGenerationService.generateCoverLetter(4, validRequest)

        assertTrue { result.isFailure }
        coVerify(exactly = 0) { typstService.compile(any(), any(), any()) }
    }

    @Test
    suspend fun testGenerateCoverLetterWithMissingProfilePicture() {
        every { profilePictureService.getCVPicture(eq(1), any()) } returns Result.failure(IllegalArgumentException())

        val result = coverLetterGenerationService.generateCoverLetter(1, validRequest)

        assertTrue { result.isFailure }
        coVerify(exactly = 0) { typstService.compile(any(), any(), any()) }
    }

    @Test
    suspend fun testGenerateCoverLetterConfigJsonOutput() {
        val slot = slot<Any>()
        every { objectMapper.writeValue(any<File>(), capture(slot)) } throws IllegalArgumentException()
        every { profilePictureService.getCVPicture(eq(1), any()) } returns Result.success(
            ProfilePicture(
                "profile.jpg",
                profilePictureUri
            )
        )

        val result = coverLetterGenerationService.generateCoverLetter(1, validRequest)
        assertTrue { result.isFailure }
        coVerify(exactly = 0) { typstService.compile(any(), any(), any()) }

        assertNotNull(slot.captured)
        assertTrue { slot.captured is CoverLetterData }
        val data = slot.captured as CoverLetterData
        assertEquals(validRequest.language, data.language)
        assertEquals(validRequest.mirrorProfileImage, data.mirrorProfileImage)
        assertEquals(validRequest.documents, data.documents)
        assertEquals("Developer", data.author.jobTitle)
        assertEquals("First", data.author.firstName)
        assertEquals("Last", data.author.lastName)
        assertEquals("123456789", data.author.phone)
        assertEquals("a@b.c", data.author.email)
        assertEquals("street 5, 123 city", data.author.address)
        assertEquals(validRequest.application!!.jobTitle, data.application.jobTitle)
        if (validRequest.application.contactPerson == null) {
            assertEquals(validRequest.application.addressee, data.application.addressee)
        } else {
            assertNotNull(data.application.contactPerson)
            assertEquals(validRequest.application.contactPerson.firstName, data.application.contactPerson.firstName)
            assertEquals(validRequest.application.contactPerson.lastName, data.application.contactPerson.lastName)
        }
        assertEquals(validRequest.application.companyAddress!!.street, data.application.companyAddress.line1)
        assertEquals(
            "${validRequest.application.companyAddress.postcode} ${validRequest.application.companyAddress.city}",
            data.application.companyAddress.line2
        )
        assertEquals(validRequest.application.salutation, data.application.salutation)
        assertEquals(validRequest.application.content, data.application.content)
        assertEquals(validRequest.application.closing, data.application.closing)
        assertEquals(validRequest.application.company, data.application.company)
    }

    @Test
    suspend fun testGenerateCoverLetterWithFailingCompilation() {
        every { profilePictureService.getCVPicture(eq(1), any()) } returns Result.success(
            ProfilePicture(
                "profile.jpg",
                profilePictureUri
            )
        )
        every { objectMapper.writeValue(any<File>(), any()) } just Runs
        coEvery { typstService.compile(any(), any(), any()) } returns Result.failure(IllegalArgumentException())

        val result = coverLetterGenerationService.generateCoverLetter(1, validRequest)

        assertTrue { result.isFailure }
        coVerify(exactly = 1) { typstService.compile(any(), any(), any()) }
    }

    @Test
    suspend fun testGenerateCoverLetterSuccess() {
        every { profilePictureService.getCVPicture(eq(1), any()) } returns Result.success(
            ProfilePicture(
                "profile.jpg",
                profilePictureUri
            )
        )
        every { objectMapper.writeValue(any<File>(), any()) } just Runs
        coEvery { typstService.compile(any(), any(), any()) } returns Result.success(ByteArray(10))

        val result = coverLetterGenerationService.generateCoverLetter(1, validRequest)
        assertTrue { result.isSuccess }
        coVerify(exactly = 1) { typstService.compile(any(), any(), any()) }
    }
}