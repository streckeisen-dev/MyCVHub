package ch.streckeisen.mycv.backend.account.verification

import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.email.EmailService
import ch.streckeisen.mycv.backend.email.EmailTemplateException
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

private const val VERIFICATION_TOKEN_EXPIRATION_HOURS = 2L
private const val TOKEN_GENERATION_BLOCK_MINUTES = 5L

class AccountVerificationServiceTest {
    private lateinit var accountVerificationRepository: AccountVerificationRepository
    private lateinit var emailService: EmailService
    private lateinit var applicantAccountRepository: ApplicantAccountRepository
    private lateinit var accountVerificationService: AccountVerificationService

    @BeforeEach
    fun setup() {
        accountVerificationRepository = mockk {
            every { findByAccountId(any()) } returns Optional.empty()
            every { save(any()) } returns mockk()
            every { deleteById(any()) } just runs
        }
        emailService = mockk {
            every { sendAccountVerificationEmail(any(), any()) } returns Result.success(Unit)
        }
        applicantAccountRepository = mockk {
            every { findById(any()) } returns Optional.empty()
            every { findById(eq(1)) } returns Optional.of(mockk {
                every { id } returns 1
                every { isVerified } returns false
            })
            every { findById(eq(2)) } returns Optional.of(mockk {
                every { id } returns 2
                every { isVerified } returns true
            })
            every { setAccountVerified(any()) } just runs
        }

        accountVerificationService = AccountVerificationService(
            accountVerificationRepository,
            emailService,
            applicantAccountRepository,
            VERIFICATION_TOKEN_EXPIRATION_HOURS,
            TOKEN_GENERATION_BLOCK_MINUTES
        )
    }

    @Test
    fun testVerifyToken() {
        val token = UUID.randomUUID().toString()
        every { accountVerificationRepository.findByAccountId(eq(1)) } returns Optional.of(
            AccountVerificationEntity(
                token,
                LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRATION_HOURS / 1),
                mockk { every { id } returns 1 },
                1
            )
        )

        val result = accountVerificationService.verifyToken(1, token)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { applicantAccountRepository.setAccountVerified(eq(1)) }
        verify(exactly = 1) { accountVerificationRepository.deleteById(eq(1)) }
    }

    @Test
    fun testVerifyTokenWithNoToken() {
        every { accountVerificationRepository.findByAccountId(eq(1)) } returns Optional.empty()

        val result = accountVerificationService.verifyToken(1, UUID.randomUUID().toString())

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        verify(exactly = 0) { applicantAccountRepository.setAccountVerified(any()) }
    }

    @Test
    fun testVerifyTokenWithNoAccount() {
        val result = accountVerificationService.verifyToken(2, UUID.randomUUID().toString())

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        verify(exactly = 0) { applicantAccountRepository.setAccountVerified(any()) }
    }

    @Test
    fun testVerifyTokenWithExpiredToken() {
        val token = UUID.randomUUID().toString()
        every { accountVerificationRepository.findByAccountId(eq(1)) } returns Optional.of(
            AccountVerificationEntity(
                token,
                LocalDateTime.now().minusHours(VERIFICATION_TOKEN_EXPIRATION_HOURS + 1),
                mockk { every { id } returns 1 }
            )
        )

        val result = accountVerificationService.verifyToken(1, token)

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        verify(exactly = 0) { applicantAccountRepository.setAccountVerified(any()) }
    }

    @Test
    fun testVerifyTokenWithWrongToken() {
        every { accountVerificationRepository.findByAccountId(eq(1)) } returns Optional.of(
            AccountVerificationEntity(
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusMinutes(30),
                mockk { every { id } returns 1 }
            )
        )

        val result = accountVerificationService.verifyToken(1, "abc")

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        verify(exactly = 0) { accountVerificationRepository.deleteById(any()) }
        verify(exactly = 0) { applicantAccountRepository.setAccountVerified(any()) }
    }

    @Test
    fun testGenerateVerificationToken() {
        every { emailService.sendAccountVerificationEmail(any(), any()) } returns Result.success(Unit)

        val result = accountVerificationService.generateVerificationToken(1)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { accountVerificationRepository.save(any()) }
        verify(exactly = 1) { emailService.sendAccountVerificationEmail(any(), any()) }
    }

    @Test
    fun testGenerateVerificationTokenWithNoAccount() {
        val result = accountVerificationService.generateVerificationToken(5)

        assertTrue(result.isFailure)
        verify(exactly = 0) { accountVerificationRepository.save(any()) }
        verify(exactly = 0) { emailService.sendAccountVerificationEmail(any(), any()) }
    }

    @Test
    fun testGenerateVerificationTokenWithVerifiedAccount() {
        val result = accountVerificationService.generateVerificationToken(2)

        assertTrue(result.isFailure)
        verify(exactly = 0) { accountVerificationRepository.save(any()) }
        verify(exactly = 0) { emailService.sendAccountVerificationEmail(any(), any()) }
    }

    @Test
    fun testGenerateVerificationTokenWithExistingBlockingToken() {
        every { accountVerificationRepository.findByAccountId(eq(1)) } returns Optional.of(
            AccountVerificationEntity(
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRATION_HOURS)
                    .minusMinutes(TOKEN_GENERATION_BLOCK_MINUTES - 1),
                mockk { every { id } returns 1 }
            )
        )

        val result = accountVerificationService.generateVerificationToken(1)

        assertTrue(result.isFailure)
        verify(exactly = 0) { accountVerificationRepository.save(any()) }
        verify(exactly = 0) { emailService.sendAccountVerificationEmail(any(), any()) }
    }

    @Test
    fun testGenerateVerificationTokenWithExistingNonBlockingToken() {
        every { accountVerificationRepository.findByAccountId(eq(1)) } returns Optional.of(
            AccountVerificationEntity(
                UUID.randomUUID().toString(),
                LocalDateTime.now().minusMinutes(TOKEN_GENERATION_BLOCK_MINUTES + 1),
                mockk { every { id } returns 1 }
            )
        )

        val result = accountVerificationService.generateVerificationToken(1)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { accountVerificationRepository.save(any()) }
        verify(exactly = 1) { emailService.sendAccountVerificationEmail(any(), any()) }
    }

    @Test
    fun testGenerateVerificationTokenWithEmailError() {
        every { emailService.sendAccountVerificationEmail(any(), any()) } returns Result.failure(
            EmailTemplateException(
                "",
                mockk()
            )
        )

        val result = accountVerificationService.generateVerificationToken(1)

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is EmailTemplateException)
        verify(exactly = 1) { accountVerificationRepository.save(any()) }
    }
}