package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import ch.streckeisen.mycv.backend.cv.applicant.Applicant
import ch.streckeisen.mycv.backend.cv.applicant.ApplicantRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val applicantRepository: ApplicantRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String?): MyCvUserDetails {
        if (username.isNullOrBlank()) {
            throw IllegalArgumentException("Username cannot be null or blank")
        }
        val applicant: Applicant = applicantRepository.findByEmail(username)
            .orElseThrow { ResultNotFoundException("There is no user with username $username") }
        val userDetails = User.withUsername(applicant.email)
            .password(applicant.password)
            .accountLocked(false)
            .accountExpired(false)
            .disabled(false)
            .credentialsExpired(false)
            .build()
        return MyCvUserDetails(userDetails, applicant.id!!)
    }
}