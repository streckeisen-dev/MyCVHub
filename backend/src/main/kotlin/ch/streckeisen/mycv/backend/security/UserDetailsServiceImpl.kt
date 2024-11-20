package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class UserDetailsServiceImpl(
    private val applicantAccountRepository: ApplicantAccountRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String?): MyCvUserDetails {
        return loadUserByUsernameAsResult(username).getOrThrow()
    }

    fun loadUserByUsernameAsResult(username: String?): Result<MyCvUserDetails> {
        if (username.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Username cannot be null or blank"))
        }
        val applicantAccount: ApplicantAccountEntity = applicantAccountRepository.findByUsername(username)
            .getOrElse { return Result.failure(UsernameNotFoundException("There is no user with username $username")) }

        return Result.success(MyCvUserDetails(applicantAccount))
    }
}