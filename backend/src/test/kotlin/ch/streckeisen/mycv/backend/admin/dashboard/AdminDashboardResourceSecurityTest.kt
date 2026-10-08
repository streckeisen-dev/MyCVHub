package ch.streckeisen.mycv.backend.admin.dashboard

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.account.auth.ACCESS_TOKEN_NAME
import ch.streckeisen.mycv.backend.account.auth.AuthTokenService
import ch.streckeisen.mycv.backend.admin.account.AdminAccountEntity
import ch.streckeisen.mycv.backend.admin.account.AdminAccountRepository
import ch.streckeisen.mycv.backend.admin.account.AdminRole
import ch.streckeisen.mycv.backend.admin.auth.ADMIN_ACCESS_TOKEN_NAME
import ch.streckeisen.mycv.backend.admin.auth.AdminAuthTokenService
import com.github.kagkarlsson.scheduler.Scheduler
import io.mockk.mockk
import jakarta.servlet.Filter
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime

private const val ACTIVE_ADMIN_USERNAME = "active-admin@example.com"
private const val TEMPORARY_ADMIN_USERNAME = "temporary-admin@example.com"
private const val APPLICANT_USERNAME = "applicant@example.com"

@SpringBootTest(properties = ["db-scheduler.enabled=false"])
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class AdminDashboardResourceSecurityTest {
    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private lateinit var springSecurityFilterChain: Filter

    @Autowired
    private lateinit var adminAccountRepository: AdminAccountRepository

    @Autowired
    private lateinit var applicantAccountRepository: ApplicantAccountRepository

    @Autowired
    private lateinit var adminAuthTokenService: AdminAuthTokenService

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        val builder = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        builder.addFilters<DefaultMockMvcBuilder>(springSecurityFilterChain)
        mockMvc = builder.build()
        adminAccountRepository.deleteAll()
        applicantAccountRepository.deleteAll()
        seedAdmin(ACTIVE_ADMIN_USERNAME, mustChangePassword = false)
        seedAdmin(TEMPORARY_ADMIN_USERNAME, mustChangePassword = true)
        seedApplicant()
    }

    @Test
    fun dashboardRejectsAnonymousRequests() {
        mockMvc.perform(get("/api/admin/dashboard"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun dashboardRejectsApplicantAccessTokens() {
        val accessToken = authTokenService.generateAuthData(APPLICANT_USERNAME).getOrThrow().accessToken

        mockMvc.perform(
            get("/api/admin/dashboard")
                .cookie(Cookie(ACCESS_TOKEN_NAME, accessToken))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun dashboardRejectsAdminsWhoMustChangePassword() {
        val accessToken = adminAuthTokenService.generateAuthData(TEMPORARY_ADMIN_USERNAME).getOrThrow().accessToken

        mockMvc.perform(
            get("/api/admin/dashboard")
                .cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, accessToken))
        ).andExpect(status().isForbidden)
    }

    @Test
    fun dashboardAllowsActiveAdmins() {
        val accessToken = adminAuthTokenService.generateAuthData(ACTIVE_ADMIN_USERNAME).getOrThrow().accessToken

        mockMvc.perform(
            get("/api/admin/dashboard")
                .cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, accessToken))
        ).andExpect(status().isOk)
    }

    private fun seedAdmin(username: String, mustChangePassword: Boolean) {
        adminAccountRepository.save(
            AdminAccountEntity(
                username = username,
                password = "encoded-password",
                role = AdminRole.ADMIN,
                isActive = true,
                mustChangePassword = mustChangePassword,
                temporaryPasswordExpiresAt = if (mustChangePassword) LocalDateTime.now().plusMinutes(10) else null
            )
        )
    }

    private fun seedApplicant() {
        applicantAccountRepository.save(
            ApplicantAccountEntity(
                username = APPLICANT_USERNAME,
                password = "encoded-password",
                isOAuthUser = false,
                isVerified = true,
                accountDetails = AccountDetailsEntity(
                    firstName = "Applicant",
                    lastName = "User",
                    email = APPLICANT_USERNAME,
                    phone = "+41790000000",
                    birthday = LocalDate.of(1990, 1, 1),
                    street = "Main Street",
                    houseNumber = "1",
                    postcode = "8000",
                    city = "Zurich",
                    country = "CH",
                    language = "en"
                )
            )
        )
    }

    @TestConfiguration
    class SchedulerTestConfiguration {
        @Bean
        fun scheduler(): Scheduler = mockk(relaxed = true)
    }
}
