package ch.streckeisen.mycv.backend.admin.auth

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AdminLoginRateLimitProperties::class)
class AdminAuthConfiguration
