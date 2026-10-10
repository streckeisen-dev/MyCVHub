package ch.streckeisen.mycv.backend.admin.bootstrap

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
@Profile("admin-bootstrap")
class AdminBootstrapRunner(
    private val adminBootstrapProperties: AdminBootstrapProperties,
    private val adminBootstrapService: AdminBootstrapService
) : CommandLineRunner {
    override fun run(vararg args: String) {
        val username = adminBootstrapProperties.username?.trim()
        if (username.isNullOrBlank()) {
            System.err.println("Missing required property: my-cv.admin.bootstrap.username")
            exitProcess(1)
        }

        try {
            val result = adminBootstrapService.bootstrap(username, adminBootstrapProperties.resetExisting)
            println()
            println("Temporary admin password for ${result.username}:")
            println(result.temporaryPassword)
            println()
            println("Expires at: ${result.expiresAt}")
            println("This password will not be shown again.")
        } catch (ex: Exception) {
            System.err.println("Admin bootstrap failed: ${ex.message}")
            exitProcess(1)
        }
    }
}
