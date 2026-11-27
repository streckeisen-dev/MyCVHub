package ch.streckeisen.mycv.backend.admin.users

import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.admin.toAdminOffsetDateTime
import ch.streckeisen.mycv.backend.admin.account.AdminRole
import ch.streckeisen.mycv.backend.security.annotations.RequiresAdminRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

data class AdminUserDto(
    val username: String,
    val email: String?,
    val createdAt: OffsetDateTime?,
    val lastLoginAt: OffsetDateTime?
)

@RestController
@RequestMapping("/api/admin/users")
@RequiresAdminRole(role = AdminRole.ADMIN, exact = true)
class AdminUserResource(private val applicantAccountRepository: ApplicantAccountRepository) {
    @GetMapping
    @Transactional(readOnly = true)
    fun listUsers(
        @RequestParam(defaultValue = "") search: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "25") size: Int
    ): Page<AdminUserDto> = applicantAccountRepository
        .findRegisteredUsers(search.trim().take(100), PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100)))
        .map { AdminUserDto(it.username, it.accountDetails?.email, it.createdAt?.toAdminOffsetDateTime(), it.lastLoginAt?.toAdminOffsetDateTime()) }
}
