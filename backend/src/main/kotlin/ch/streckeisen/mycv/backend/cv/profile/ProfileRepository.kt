package ch.streckeisen.mycv.backend.cv.profile

import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface ProfileRepository : CrudRepository<ProfileEntity, Long> {
    fun findByAlias(alias: String): Optional<ProfileEntity>

    fun findByAccountId(accountId: Long): Optional<ProfileEntity>
}