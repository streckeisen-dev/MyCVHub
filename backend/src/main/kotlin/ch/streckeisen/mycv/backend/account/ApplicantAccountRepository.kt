package ch.streckeisen.mycv.backend.account

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface ApplicantAccountRepository : CrudRepository<ApplicantAccountEntity, Long> {
    fun findByUsername(username: String): Optional<ApplicantAccountEntity>

    @Query("SELECT a FROM ApplicantAccountEntity a WHERE a.accountDetails.email = :email")
    fun findByEmail(email: String): Optional<ApplicantAccountEntity>

    @Modifying
    @Query("UPDATE ApplicantAccountEntity a SET a.isVerified = true WHERE a.id = :accountId")
    fun setAccountVerified(accountId: Long)

    @Query("SELECT a.isVerified FROM ApplicantAccountEntity a WHERE a.id = :accountId")
    fun isAccountVerified(accountId: Long): Optional<Boolean>

    @Query("SELECT a.accountDetails IS NOT NULL FROM ApplicantAccountEntity a WHERE a.id = :accountId")
    fun hasAccountDetails(accountId: Long): Optional<Boolean>

    @Modifying
    @Query("UPDATE ApplicantAccountEntity a SET a.password = :password WHERE a.id = :accountId")
    fun setPassword(accountId: Long, password: String): Optional<ApplicantAccountEntity>
}