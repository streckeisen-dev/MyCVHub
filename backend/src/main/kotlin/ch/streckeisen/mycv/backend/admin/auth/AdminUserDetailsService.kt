package ch.streckeisen.mycv.backend.admin.auth

import ch.streckeisen.mycv.backend.admin.account.AdminAccountRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class AdminUserDetailsService(
    private val adminAccountRepository: AdminAccountRepository
) {
    fun loadAdminByUsernameAsResult(username: String?): Result<AdminUserDetails> {
        if (username.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Username cannot be null or blank"))
        }
        val admin = adminAccountRepository.findByUsername(username)
            .getOrElse { return Result.failure(UsernameNotFoundException("There is no admin with username $username")) }

        return Result.success(AdminUserDetails(admin))
    }
}
