package ch.streckeisen.mycv.backend.application

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ApplicationRepository : JpaRepository<ApplicationEntity, Long> {
    @Query(
        """
            SELECT a
            FROM ApplicationEntity a
            WHERE a.account.id = :accountId 
                AND (:searchTerm IS NULL OR a.jobTitle LIKE %:searchTerm% OR a.company LIKE %:searchTerm%)
                AND (:status IS NULL OR a.status = :status)"""
    )
    fun searchByAccountId(
        @Param("accountId") accountId: Long,
        @Param("searchTerm") searchTerm: String? = null,
        @Param("status") status: ApplicationStatus? = null,
        pageable: Pageable
    ): Page<ApplicationEntity>

    @Query(
        """
        SELECT NEW ch.streckeisen.mycv.backend.application.ApplicationStat(a.status, COUNT(a))
        FROM ApplicationEntity a
        WHERE a.account.id = :accountId
        GROUP BY a.status
    """
    )
    fun getApplicationStats(@Param("accountId") accountId: Long): List<ApplicationStat>
}