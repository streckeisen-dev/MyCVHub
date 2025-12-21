package ch.streckeisen.mycv.backend.application

import org.springframework.data.jpa.repository.JpaRepository

interface ApplicationHistoryRepository : JpaRepository<ApplicationHistoryEntity, Long>