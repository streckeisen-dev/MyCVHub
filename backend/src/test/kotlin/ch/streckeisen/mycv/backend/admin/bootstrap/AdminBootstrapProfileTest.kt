package ch.streckeisen.mycv.backend.admin.bootstrap

import ch.streckeisen.mycv.backend.admin.auth.AdminJwtAuthenticationFilter
import ch.streckeisen.mycv.backend.security.ApplicationSecurityConfig
import ch.streckeisen.mycv.backend.security.JwtAuthenticationFilter
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class AdminBootstrapProfileTest {
    private val contextRunner = ApplicationContextRunner()
        .withInitializer { context -> context.environment.setActiveProfiles("admin-bootstrap") }
        .withUserConfiguration(
            AdminJwtAuthenticationFilter::class.java,
            ApplicationSecurityConfig::class.java,
            JwtAuthenticationFilter::class.java
        )

    @Test
    fun adminBootstrapProfileDoesNotCreateWebSecurityBeans() {
        contextRunner.run { context ->
            assertTrue(context.getBeansOfType(AdminJwtAuthenticationFilter::class.java).isEmpty())
            assertTrue(context.getBeansOfType(ApplicationSecurityConfig::class.java).isEmpty())
            assertTrue(context.getBeansOfType(JwtAuthenticationFilter::class.java).isEmpty())
        }
    }
}
