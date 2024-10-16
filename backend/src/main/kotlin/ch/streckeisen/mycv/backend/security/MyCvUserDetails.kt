package ch.streckeisen.mycv.backend.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class MyCvUserDetails(
    private val userDetails: UserDetails,
    val applicantId: Long
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority?>? {
        return userDetails.authorities
    }

    override fun getPassword(): String? {
        return userDetails.password
    }

    override fun getUsername(): String? {
        return userDetails.username
    }

    override fun isAccountNonExpired(): Boolean {
        return userDetails.isAccountNonExpired
    }

    override fun isAccountNonLocked(): Boolean {
        return userDetails.isAccountNonLocked
    }

    override fun isCredentialsNonExpired(): Boolean {
        return userDetails.isCredentialsNonExpired
    }

    override fun isEnabled(): Boolean {
        return userDetails.isEnabled
    }
}