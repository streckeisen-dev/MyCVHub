package ch.streckeisen.mycv.backend.admin.auth

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface AdminLoginAttemptRepository : JpaRepository<AdminLoginAttemptEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select attempt from AdminLoginAttemptEntity attempt where attempt.username = :username")
    fun findByUsernameForUpdate(@Param("username") username: String): Optional<AdminLoginAttemptEntity>
}
