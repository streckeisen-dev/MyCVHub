package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val applicantAccountRepository: ApplicantAccountRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String?): MyCvUserDetails {
        if (username.isNullOrBlank()) {
            throw IllegalArgumentException("Username cannot be null or blank")
        }
        val applicantAccount: ApplicantAccountEntity = applicantAccountRepository.findByEmail(username)
            .orElseThrow { ResultNotFoundException("There is no user with username $username") }
        val userDetails = User.withUsername(applicantAccount.email)
            .password(applicantAccount.password)
            .accountLocked(false)
            .accountExpired(false)
            .disabled(false)
            .credentialsExpired(false)
            .build()
        return MyCvUserDetails(userDetails, applicantAccount.id!!)
    }
}