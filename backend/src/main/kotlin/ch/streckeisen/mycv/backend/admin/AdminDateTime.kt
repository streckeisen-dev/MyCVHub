package ch.streckeisen.mycv.backend.admin

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/** Existing timestamps use the server's local timezone; retain their meaning in API responses. */
fun LocalDateTime.toAdminOffsetDateTime(sourceZone: ZoneId = ZoneId.systemDefault()): OffsetDateTime =
    atZone(sourceZone).withZoneSameInstant(ZoneOffset.ofHours(1)).toOffsetDateTime()
