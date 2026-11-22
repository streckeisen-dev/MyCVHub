package ch.streckeisen.mycv.backend.account

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.Optional
import java.time.LocalDateTime
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ApplicantAccountRepository : CrudRepository<ApplicantAccountEntity, Long> {
    @Query("""
        SELECT a FROM ApplicantAccountEntity a LEFT JOIN FETCH a.accountDetails d
        WHERE (:search = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(d.email) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY a.username, a.id
    """, countQuery = """
        SELECT COUNT(a) FROM ApplicantAccountEntity a LEFT JOIN a.accountDetails d
        WHERE (:search = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(d.email) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    fun findRegisteredUsers(search: String, pageable: Pageable): Page<ApplicantAccountEntity>

    @Transactional
    @Modifying
    @Query("UPDATE ApplicantAccountEntity a SET a.lastLoginAt = :occurredAt WHERE a.id = :accountId AND (a.lastLoginAt IS NULL OR a.lastLoginAt < :occurredAt)")
    fun updateLastLoginAt(accountId: Long, occurredAt: LocalDateTime): Int

    fun findByUsername(username: String): Optional<ApplicantAccountEntity>

    fun countByIsVerified(isVerified: Boolean): Long

    fun countByIsOAuthUser(isOAuthUser: Boolean): Long

    @Query("SELECT COUNT(a) FROM ApplicantAccountEntity a WHERE a.accountDetails IS NOT NULL AND a.isVerified = false")
    fun countUnverifiedCompleteAccounts(): Long

    @Query("SELECT a FROM ApplicantAccountEntity a LEFT JOIN FETCH a.accountDetails WHERE a.username = :username")
    fun findByUsernameWithDetails(username: String): Optional<ApplicantAccountEntity>

    @Query("SELECT a FROM ApplicantAccountEntity a WHERE a.accountDetails.email = :email")
    fun findByEmail(email: String): Optional<ApplicantAccountEntity>

    @Modifying
    @Query("UPDATE ApplicantAccountEntity a SET a.isVerified = true WHERE a.id = :accountId")
    fun setAccountVerified(accountId: Long)

    @Query("SELECT a.isVerified FROM ApplicantAccountEntity a WHERE a.id = :accountId")
    fun isAccountVerified(accountId: Long): Optional<Boolean>

    @Query("SELECT a.accountDetails IS NOT NULL FROM ApplicantAccountEntity a WHERE a.id = :accountId")
    fun hasAccountDetails(accountId: Long): Optional<Boolean>

    @Modifying
    @Query("UPDATE ApplicantAccountEntity a SET a.password = :password WHERE a.id = :accountId")
    fun setPassword(accountId: Long, password: String): Optional<ApplicantAccountEntity>
}
