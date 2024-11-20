package ch.streckeisen.mycv.backend.account.verification

import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface AccountVerificationRepository: CrudRepository<AccountVerificationEntity, Long> {
    fun findByAccountId(accountId: Long): Optional<AccountVerificationEntity>
}