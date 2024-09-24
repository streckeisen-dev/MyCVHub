package ch.streckeisen.mycv.backend.account

data class AuthData (
    val accessToken: String,
    val accessTokenExpirationTime: Long,
    val refreshToken: String,
    val refreshTokenExpirationTime: Long
)