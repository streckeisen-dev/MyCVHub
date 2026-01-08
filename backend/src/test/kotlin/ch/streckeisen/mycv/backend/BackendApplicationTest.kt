package ch.streckeisen.mycv.backend

import com.github.kagkarlsson.scheduler.Scheduler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class BackendApplicationTest {
    @Autowired
    private lateinit var scheduler: Scheduler

    @Test
    fun schedulerIsAvailable() {
        assertNotNull(scheduler)
    }
}