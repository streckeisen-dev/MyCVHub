package ch.streckeisen.mycv.backend.upgrade

import org.springframework.data.jpa.repository.JpaRepository

interface UpgradeTaskExecutionRepository : JpaRepository<UpgradeTaskExecutionEntity, Int>