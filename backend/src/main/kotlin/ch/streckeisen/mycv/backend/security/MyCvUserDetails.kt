package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class MyCvUserDetails(
    val account: ApplicantAccountEntity
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf()
    }

    override fun getPassword(): String? {
        return account.password
    }

    override fun getUsername(): String {
        return account.username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}