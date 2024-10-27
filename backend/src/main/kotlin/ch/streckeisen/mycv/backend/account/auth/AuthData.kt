package ch.streckeisen.mycv.backend.account.auth

data class AuthData(
    val accessToken: String,
    val accessTokenExpirationTime: Long,
    val refreshToken: String,
    val refreshTokenExpirationTime: Long
)