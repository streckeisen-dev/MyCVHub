package ch.streckeisen.mycv.backend.applicationTemplate

import org.springframework.data.jpa.repository.JpaRepository

interface ApplicationTemplateRepository: JpaRepository<ApplicationTemplateEntity, Long> {
    fun findByAccountId(accountId: Long): List<ApplicationTemplateEntity>
}