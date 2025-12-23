package ch.streckeisen.mycv.backend.cv.profile

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.Optional

interface ProfileRepository : CrudRepository<ProfileEntity, Long> {
    @Query("SELECT p FROM ProfileEntity p WHERE p.account.username = :username")
    fun findByAccountUsername(@Param("username") username: String): Optional<ProfileEntity>

    fun findByAccountId(accountId: Long): Optional<ProfileEntity>

    @Query("SELECT NEW ch.streckeisen.mycv.backend.cv.profile.ProfileStats(size(p.workExperiences), size(p.education), size(p.projects), size(p.skills)) FROM ProfileEntity p WHERE p.account.id = :accountId")
    fun getProfileStats(@Param("accountId") accountId: Long): Optional<ProfileStats>
}