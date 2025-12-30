package ch.streckeisen.mycv.backend.scheduled.tasks

import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceService
import ch.streckeisen.mycv.backend.scheduled.data.AddWorkExperienceEntryTaskData
import com.github.kagkarlsson.scheduler.task.TaskDescriptor
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask
import com.github.kagkarlsson.scheduler.task.helper.Tasks
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalTime

val ADD_WORK_EXPERIENCE_TASK_DESCRIPTOR: TaskDescriptor<AddWorkExperienceEntryTaskData> =
    TaskDescriptor.of("scheduled-work-experience", AddWorkExperienceEntryTaskData::class.java)
val ADD_WORK_EXPERIENCE_TASK_RUN_TIME: LocalTime = LocalTime.of(1, 15, 0)

private val logger = KotlinLogging.logger {}

@Configuration
class AddWorkExperienceTaskConfiguration(
    private val workExperienceService: WorkExperienceService
) {

    @Bean
    fun addScheduledWorkExperienceTask(): OneTimeTask<AddWorkExperienceEntryTaskData> {
        return Tasks.oneTime(ADD_WORK_EXPERIENCE_TASK_DESCRIPTOR)
            .execute { instance, _ ->
                val data: AddWorkExperienceEntryTaskData = instance.data

                logger.info { "Adding scheduled cv entry for account ${data.accountId}" }

                val updateRequest = data.toUpdateRequest()
                workExperienceService.save(data.accountId, updateRequest, true)
                    .onFailure { logger.error(it) { "Failed to add cv entry for account ${data.accountId}" } }
            }
    }
}

