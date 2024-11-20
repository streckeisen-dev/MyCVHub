package ch.streckeisen.mycv.backend.cv.profile

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.Optional

interface ProfileRepository : CrudRepository<ProfileEntity, Long> {
    @Query("SELECT p FROM ProfileEntity p WHERE p.account.username = :username")
    fun findByAccountUsername(@Param("username") username: String): Optional<ProfileEntity>

    fun findByAccountId(accountId: Long): Optional<ProfileEntity>
}