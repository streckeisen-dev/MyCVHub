package ch.streckeisen.mycv.backend.upgrade.tasks

import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.account.verification.AccountVerificationService
import ch.streckeisen.mycv.backend.upgrade.UpgradeTask
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger { }

@Configuration
class AccountVerificationUpgrade(
    private val accountVerificationService: AccountVerificationService,
    private val accountRepository: ApplicantAccountRepository
) : UpgradeTask(
    id = 1,
    name = "Account Verification Upgrade"
) {
    override fun execute(): Result<Unit> {
        accountRepository.findAll().forEach { account ->
            accountVerificationService.generateVerificationToken(account.id!!)
                .onFailure {
                    // the upgrade is still considered successful if the token generation fails for a user
                    logger.error(it) {
                        "Failed to generate verification token for account ${account.id}"
                    }
                }
        }
        return Result.success(Unit)
    }
}