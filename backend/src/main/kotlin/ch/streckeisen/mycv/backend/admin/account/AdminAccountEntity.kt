package ch.streckeisen.mycv.backend.admin.account

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class AdminAccountEntity(
    @Column(unique = true)
    var username: String,
    var password: String,
    @Enumerated(EnumType.STRING)
    var role: AdminRole,
    @Column(name = "is_active")
    var isActive: Boolean,
    var mustChangePassword: Boolean,
    var temporaryPasswordExpiresAt: LocalDateTime? = null,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var lastLoginAt: LocalDateTime? = null,
    var passwordChangedAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)
