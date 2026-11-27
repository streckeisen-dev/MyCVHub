package ch.streckeisen.mycv.backend.admin.account

import org.springframework.data.repository.CrudRepository
import java.util.Optional
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface AdminAccountRepository : CrudRepository<AdminAccountEntity, Long> {
    fun findByUsername(username: String): Optional<AdminAccountEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AdminAccountEntity a WHERE a.username = :username")
    fun findByUsernameForUpdate(username: String): Optional<AdminAccountEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AdminAccountEntity a WHERE a.id = :id")
    fun findByIdForUpdate(id: Long): Optional<AdminAccountEntity>
}
