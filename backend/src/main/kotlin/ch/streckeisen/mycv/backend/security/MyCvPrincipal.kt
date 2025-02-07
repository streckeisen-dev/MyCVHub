package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.AccountStatus

data class MyCvPrincipal(
    val username: String,
    val id: Long,
    val status: AccountStatus
)
