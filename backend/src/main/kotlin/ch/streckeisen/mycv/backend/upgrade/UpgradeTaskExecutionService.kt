package ch.streckeisen.mycv.backend.upgrade

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UpgradeTaskExecutionService(
    private val upgradeTaskExecutionRepository: UpgradeTaskExecutionRepository
) {
    @Transactional(readOnly = true)
    fun wasTaskExecuted(task: UpgradeTask): Boolean {
        return upgradeTaskExecutionRepository.existsById(task.id)
    }

    @Transactional
    fun setUpgradeTaskAsExecuted(task: UpgradeTask): UpgradeTaskExecutionEntity {
        return upgradeTaskExecutionRepository.save(UpgradeTaskExecutionEntity(task.id, task.name, LocalDateTime.now()))
    }
}