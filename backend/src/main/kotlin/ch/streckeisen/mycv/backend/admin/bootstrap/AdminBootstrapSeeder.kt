package ch.streckeisen.mycv.backend.admin.bootstrap

import ch.streckeisen.mycv.backend.admin.account.AdminAccountEntity
import ch.streckeisen.mycv.backend.admin.account.AdminAccountRepository
import ch.streckeisen.mycv.backend.admin.account.AdminRole
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

private val logger = KotlinLogging.logger {}

private const val UNUSABLE_BOOTSTRAP_PASSWORD = "admin-bootstrap-unusable-password"

@Configuration
@Profile("!admin-bootstrap")
class AdminBootstrapSeederConfiguration(
    private val adminBootstrapSeeder: AdminBootstrapSeeder
) {
    @Bean
    fun seedInitialSuperAdmin(): ApplicationRunner = ApplicationRunner {
        adminBootstrapSeeder.seedInitialSuperAdmin()
    }
}

@Component
class AdminBootstrapSeeder(
    private val adminAccountRepository: AdminAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val adminBootstrapProperties: AdminBootstrapProperties
) {
    @Transactional
    fun seedInitialSuperAdmin() {
        val username = adminBootstrapProperties.seedUsername?.trim().orEmpty()
        if (username.isBlank()) {
            return
        }

        val existingAdmin = adminAccountRepository.findByUsername(username).getOrNull()
        if (existingAdmin != null) {
            logger.info { "Initial super admin seed account already exists" }
            return
        }

        adminAccountRepository.save(
            AdminAccountEntity(
                username = username,
                password = passwordEncoder.encode(UNUSABLE_BOOTSTRAP_PASSWORD)!!,
                role = AdminRole.SUPER_ADMIN,
                isActive = false,
                mustChangePassword = true
            )
        )
        logger.info { "Created inactive initial super admin seed account" }
    }
}
