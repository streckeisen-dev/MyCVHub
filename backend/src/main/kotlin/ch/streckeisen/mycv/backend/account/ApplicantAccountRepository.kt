package ch.streckeisen.mycv.backend.account

import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface ApplicantAccountRepository : CrudRepository<ApplicantAccountEntity, Long> {
    fun findByUsername(username: String): Optional<ApplicantAccountEntity>
}