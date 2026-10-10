package ch.streckeisen.mycv.backend.admin.auth

data class AdminAuthTokens(
    val accessToken: String,
    val accessTokenExpirationTime: Long,
    val refreshToken: String,
    val refreshTokenExpirationTime: Long
)
