package ch.streckeisen.mycv.backend.admin.account

import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface AdminAccountRepository : CrudRepository<AdminAccountEntity, Long> {
    fun findByUsername(username: String): Optional<AdminAccountEntity>
}
