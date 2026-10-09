package ch.streckeisen.mycv.backend.security.annotations

import ch.streckeisen.mycv.backend.admin.account.AdminRole

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresAdminRole(
    val role: AdminRole = AdminRole.ADMIN,
    val exact: Boolean = false,
    val allowMustChangePassword: Boolean = false
)
