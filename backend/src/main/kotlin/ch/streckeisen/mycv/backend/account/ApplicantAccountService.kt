package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.exceptions.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Service
class ApplicantAccountService(
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val applicantAccountValidationService: ApplicantAccountValidationService
) {
    @Transactional(readOnly = true)
    fun findById(id: Long): Result<ApplicantAccountEntity> {
        return applicantAccountRepository.findById(id)
            .map { applicant -> Result.success(applicant) }
            .orElse(Result.failure(EntityNotFoundException("No applicant with ID $id")))
    }

    @Transactional
    fun update(accountId: Long, accountUpdate: AccountUpdateDto): Result<ApplicantAccountEntity> {
        val existingAccount = applicantAccountRepository.findById(accountId)
            .getOrElse { return Result.failure(EntityNotFoundException("Account does not exist")) }

        applicantAccountValidationService.validateAccountUpdate(accountId, accountUpdate)
            .onFailure { return Result.failure(it) }

        val account = ApplicantAccountEntity(
            existingAccount.username,
            existingAccount.password,
            existingAccount.isOAuthUser,
            accountDetails = AccountDetailsEntity(
                accountUpdate.firstName!!,
                accountUpdate.lastName!!,
                accountUpdate.email!!,
                accountUpdate.phone!!,
                accountUpdate.birthday!!,
                accountUpdate.street!!,
                accountUpdate.houseNumber,
                accountUpdate.postcode!!,
                accountUpdate.city!!,
                accountUpdate.country!!
            ),
            id = existingAccount.id,
            profile = existingAccount.profile
        )
        return Result.success(applicantAccountRepository.save(account))
    }
}