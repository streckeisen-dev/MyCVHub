package ch.streckeisen.mycv.backend.applicationTemplate

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ApplicationTemplateRepository: JpaRepository<ApplicationTemplateEntity, Long> {
    fun findByAccountId(accountId: Long): List<ApplicationTemplateEntity>

    fun findByAccountIdAndName(accountId: Long, name: String): Optional<ApplicationTemplateEntity>
}