package ch.streckeisen.mycv.backend.account.auth

data class AuthTokens(
    val accessToken: String,
    val accessTokenExpirationTime: Long,
    val refreshToken: String,
    val refreshTokenExpirationTime: Long
)