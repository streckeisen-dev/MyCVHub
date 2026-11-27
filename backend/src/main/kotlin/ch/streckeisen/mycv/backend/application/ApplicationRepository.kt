package ch.streckeisen.mycv.backend.application

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

private const val APPLICATION_STATUS_TYPE = "ch.streckeisen.mycv.backend.application.ApplicationStatus"

interface ApplicationRepository : JpaRepository<ApplicationEntity, Long> {
    @Query(
        """
            SELECT a
            FROM ApplicationEntity a
            WHERE a.account.id = :accountId 
                AND (:searchTerm IS NULL
                    OR LOWER(a.jobTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(a.company) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
                AND (:status IS NULL OR a.status = :status)
                AND (:includeArchived = TRUE OR a.isArchived = FALSE)
            ORDER BY CASE
                WHEN :sortByRelevance = FALSE THEN 0
                WHEN a.isArchived = TRUE THEN 3
                WHEN a.status = $APPLICATION_STATUS_TYPE.UNSENT THEN 0
                WHEN a.status IN (
                    $APPLICATION_STATUS_TYPE.INTERVIEW_SCHEDULED, $APPLICATION_STATUS_TYPE.OFFER_RECEIVED
                ) THEN 1
                WHEN a.status IN (
                    $APPLICATION_STATUS_TYPE.WAITING_FOR_FIRST_RESPONSE,
                    $APPLICATION_STATUS_TYPE.WAITING_FOR_INTERVIEW_FEEDBACK
                ) THEN 2
                ELSE 3
            END,
            CASE WHEN :sortByRelevance = TRUE THEN COALESCE(a.updatedAt, a.createdAt) ELSE NULL END DESC
        """
    )
    fun searchByAccountId(
        @Param("accountId") accountId: Long,
        @Param("searchTerm") searchTerm: String? = null,
        @Param("status") status: ApplicationStatus? = null,
        @Param("includeArchived") includeArchived: Boolean = false,
        pageable: Pageable,
        @Param("sortByRelevance") sortByRelevance: Boolean = false
    ): Page<ApplicationEntity>

    @Query(
        """
        SELECT NEW ch.streckeisen.mycv.backend.application.ApplicationStat(a.status, COUNT(a))
        FROM ApplicationEntity a
        WHERE a.account.id = :accountId
            AND a.isArchived = FALSE
        GROUP BY a.status
    """
    )
    fun getApplicationStats(@Param("accountId") accountId: Long): List<ApplicationStat>
}
