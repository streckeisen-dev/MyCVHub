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
import ch.streckeisen.mycv.backend.admin.audit.AdminAuditLogRepository
import ch.streckeisen.mycv.backend.admin.audit.AdminAuditService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime

private const val ACTIVE_ADMIN_USERNAME = "active-admin@example.com"
private const val TEMPORARY_ADMIN_USERNAME = "temporary-admin@example.com"
private const val APPLICANT_USERNAME = "applicant@example.com"
private const val SUPER_ADMIN_USERNAME = "super-admin@example.com"

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
    private lateinit var adminAuditLogRepository: AdminAuditLogRepository

    @Autowired
    private lateinit var adminAuditService: AdminAuditService

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
        adminAuditLogRepository.deleteAll()
        adminAccountRepository.deleteAll()
        applicantAccountRepository.deleteAll()
        seedAdmin(ACTIVE_ADMIN_USERNAME, mustChangePassword = false)
        seedAdmin(TEMPORARY_ADMIN_USERNAME, mustChangePassword = true)
        adminAccountRepository.save(AdminAccountEntity(username = SUPER_ADMIN_USERNAME, password = "encoded-password", role = AdminRole.SUPER_ADMIN, isActive = true, mustChangePassword = false))
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

    @Test
    fun lastLoginUpdateKeepsTheNewestTimestamp() {
        val applicant = applicantAccountRepository.findByUsername(APPLICANT_USERNAME).orElseThrow()
        val newest = LocalDateTime.of(2026, 1, 2, 10, 0)
        applicantAccountRepository.updateLastLoginAt(applicant.id!!, newest)
        applicantAccountRepository.updateLastLoginAt(applicant.id!!, newest.minusDays(1))
        org.junit.jupiter.api.Assertions.assertEquals(newest, applicantAccountRepository.findById(applicant.id!!).orElseThrow().lastLoginAt)
    }

    @Test
    fun userListAllowsAdminsAndRejectsSuperAdmins() {
        val adminToken = adminAuthTokenService.generateAuthData(ACTIVE_ADMIN_USERNAME).getOrThrow().accessToken
        val superAdminToken = adminAuthTokenService.generateAuthData(SUPER_ADMIN_USERNAME).getOrThrow().accessToken
        mockMvc.perform(get("/api/admin/users").cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, adminToken)))
            .andExpect(status().isOk)
        mockMvc.perform(get("/api/admin/users").cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, superAdminToken)))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/api/admin/audit-log").cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, adminToken)))
            .andExpect(status().isOk)
        mockMvc.perform(get("/api/admin/audit-log").cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, superAdminToken)))
            .andExpect(status().isForbidden)
    }

    @Test
    fun adminAccountManagementRequiresSuperAdmin() {
        val adminToken = adminAuthTokenService.generateAuthData(ACTIVE_ADMIN_USERNAME).getOrThrow().accessToken
        val superToken = adminAuthTokenService.generateAuthData(SUPER_ADMIN_USERNAME).getOrThrow().accessToken
        mockMvc.perform(get("/api/admin/accounts").cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, adminToken)))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/api/admin/dashboard").cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, superToken)))
            .andExpect(status().isForbidden)
        mockMvc.perform(post("/api/admin/accounts").cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, superToken))
            .contentType(MediaType.APPLICATION_JSON).content("""{"username":"new-admin"}"""))
            .andExpect(status().isOk)
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(jsonPath("$.temporaryPassword").isNotEmpty)
        val created = adminAccountRepository.findByUsername("new-admin").orElseThrow()
        org.junit.jupiter.api.Assertions.assertTrue(created.mustChangePassword)
        adminAuditService.record(created, "ADMIN", "TEST_ACTION", "SUCCESS")
        mockMvc.perform(post("/api/admin/accounts/${created.id}/reset-password")
            .cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, superToken)))
            .andExpect(status().isOk)
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(jsonPath("$.temporaryPassword").isNotEmpty)
        mockMvc.perform(post("/api/admin/accounts/${created.id}/deactivate")
            .cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, superToken)))
            .andExpect(status().isOk)
        org.junit.jupiter.api.Assertions.assertFalse(adminAccountRepository.findById(created.id!!).orElseThrow().isActive)
        mockMvc.perform(delete("/api/admin/accounts/${created.id}").cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, adminToken)))
            .andExpect(status().isForbidden)
        mockMvc.perform(delete("/api/admin/accounts/${created.id}").cookie(Cookie(ADMIN_ACCESS_TOKEN_NAME, superToken)))
            .andExpect(status().isOk)
        org.junit.jupiter.api.Assertions.assertFalse(adminAccountRepository.findById(created.id!!).isPresent)
        val preserved = adminAuditLogRepository.findAll().first { it.action == "TEST_ACTION" }
        org.junit.jupiter.api.Assertions.assertEquals("new-admin", preserved.actorUsername)
        org.junit.jupiter.api.Assertions.assertEquals(created.id, preserved.actorIdSnapshot)
        org.junit.jupiter.api.Assertions.assertNull(preserved.admin)
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
