package ch.streckeisen.mycv.backend.admin.dashboard

import com.github.kagkarlsson.scheduler.task.helper.Tasks
import com.github.kagkarlsson.scheduler.task.schedule.Daily
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalTime

private val aggregationLogger = KotlinLogging.logger {}

@Configuration
class PlatformActivityAggregationTaskConfiguration(
    private val platformActivityAggregationService: PlatformActivityAggregationService
) {
    @Bean
    fun platformActivityAggregationTask() =
        Tasks.recurring("platform-activity-aggregation", Daily(LocalTime.of(0, 10)))
            .execute { _, _ ->
                aggregationLogger.info { "Running platform activity aggregation task" }
                platformActivityAggregationService.aggregateMissingCompletedDays()
                platformActivityAggregationService.purgeRawEvents()
            }
}
