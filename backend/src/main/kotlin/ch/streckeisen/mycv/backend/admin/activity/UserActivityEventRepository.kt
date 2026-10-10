package ch.streckeisen.mycv.backend.admin.activity

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface UserActivityEventRepository : CrudRepository<UserActivityEventEntity, Long> {
    @Query(
        """
        SELECT COUNT(DISTINCT e.applicantAccountId)
        FROM UserActivityEventEntity e
        WHERE e.eventType = :eventType
        AND e.timestamp >= :from
        AND e.timestamp < :to
        """
    )
    fun countDistinctApplicantsByEventTypeBetween(
        eventType: ActivityEventType,
        from: LocalDateTime,
        to: LocalDateTime
    ): Long

    @Query(
        """
        SELECT COUNT(e)
        FROM UserActivityEventEntity e
        WHERE e.eventType = :eventType
        AND e.timestamp >= :from
        AND e.timestamp < :to
        """
    )
    fun countEventsByEventTypeBetween(
        eventType: ActivityEventType,
        from: LocalDateTime,
        to: LocalDateTime
    ): Long

    @Query(
        """
        SELECT COUNT(DISTINCT e.applicantAccountId)
        FROM UserActivityEventEntity e
        WHERE e.eventType = :eventType
        AND e.timestamp >= :from
        """
    )
    fun countDistinctApplicantsByEventTypeSince(eventType: ActivityEventType, from: LocalDateTime): Long

    @Query("SELECT MIN(e.timestamp) FROM UserActivityEventEntity e")
    fun findOldestEventTimestamp(): LocalDateTime?

    @Modifying
    @Query("DELETE FROM UserActivityEventEntity e WHERE e.timestamp < :before")
    fun deleteByTimestampBefore(before: LocalDateTime): Int
}
