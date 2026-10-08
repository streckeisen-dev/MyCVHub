package ch.streckeisen.mycv.backend.admin.bootstrap

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AdminBootstrapProperties::class)
class AdminBootstrapConfiguration
