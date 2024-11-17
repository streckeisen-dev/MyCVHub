package ch.streckeisen.mycv.backend.upgrade

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger { }

@Component
@DependsOn("liquibase")
class UpgradeTaskExecutor(
    private val upgradeTaskExecutionService: UpgradeTaskExecutionService,
    private val upgradeTasks: List<UpgradeTask>
) {
    @PostConstruct
    fun executeUpgradeTasks() {
        upgradeTasks.forEach { upgradeTask ->
            if (!upgradeTaskExecutionService.wasTaskExecuted(upgradeTask)) {
                try {
                    logger.info { "Executing upgrade task ${upgradeTask.name}" }
                    upgradeTask.execute()
                        .onSuccess {
                            logger.info { "Upgrade task ${upgradeTask.name} executed successfully" }
                            upgradeTaskExecutionService.setUpgradeTaskAsExecuted(upgradeTask)
                        }
                        .onFailure { logger.error(it) { "Upgrade task ${upgradeTask.name} finished with error" } }
                } catch (ex: Exception) {
                    logger.error(ex) { "Upgrade task ${upgradeTask.name} finished with unhandled exception" }
                }
            }
        }
    }
}