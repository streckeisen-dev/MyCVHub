package ch.streckeisen.mycv.backend.scheduled

import ch.streckeisen.mycv.backend.scheduled.data.AddWorkExperienceEntryTaskData
import ch.streckeisen.mycv.backend.scheduled.tasks.ADD_WORK_EXPERIENCE_TASK_DESCRIPTOR
import ch.streckeisen.mycv.backend.scheduled.tasks.ADD_WORK_EXPERIENCE_TASK_RUN_TIME
import com.github.kagkarlsson.scheduler.Scheduler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class SchedulerService(
    private val scheduler: Scheduler
) {
    fun scheduleWorkExperienceAddition(data: AddWorkExperienceEntryTaskData) {
        val runDate = data.startDate.atTime(ADD_WORK_EXPERIENCE_TASK_RUN_TIME)
            .toInstant(ZoneOffset.UTC)

        scheduler.schedule(
            ADD_WORK_EXPERIENCE_TASK_DESCRIPTOR.instance(
                UUID.randomUUID().toString()
            )
                .data(data)
                .scheduledTo(runDate)
        )

        logger.info { "Account[${data.accountId}] Scheduled work experience entry addition for $runDate" }
    }
}