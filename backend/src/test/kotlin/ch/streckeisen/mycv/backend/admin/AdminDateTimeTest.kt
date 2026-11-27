package ch.streckeisen.mycv.backend.admin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

class AdminDateTimeTest {
    @Test
    fun convertsExistingServerLocalTimeToFixedOffset() {
        val serverTime = LocalDateTime.of(2026, 7, 1, 12, 0)
        assertEquals(OffsetDateTime.parse("2026-07-01T13:00:00+01:00"), serverTime.toAdminOffsetDateTime(ZoneId.of("UTC")))
        assertEquals(OffsetDateTime.parse("2026-07-01T11:00:00+01:00"), serverTime.toAdminOffsetDateTime(ZoneId.of("Europe/Zurich")))
    }
}
