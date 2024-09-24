package ch.streckeisen.mycv.backend.account

data class LoginRequestDto(
    val username: String?,
    val password: String?
) {
}