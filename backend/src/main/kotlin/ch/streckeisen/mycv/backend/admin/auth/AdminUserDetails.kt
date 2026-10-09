package ch.streckeisen.mycv.backend.admin.auth

import ch.streckeisen.mycv.backend.admin.account.AdminAccountEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AdminUserDetails(
    val admin: AdminAccountEntity
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${admin.role.name}"))
    }

    override fun getPassword(): String = admin.password

    override fun getUsername(): String = admin.username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = admin.isActive
}
