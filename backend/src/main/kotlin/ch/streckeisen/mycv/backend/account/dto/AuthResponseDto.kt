package ch.streckeisen.mycv.backend.account.dto

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.cv.profile.ThumbnailDto
import com.fasterxml.jackson.annotation.JsonProperty

data class AuthResponseDto(
    val username: String,
    val authLevel: AccountStatus,
    val displayName: String?,
    val language: String?,
    @get:JsonProperty(value = "hasProfile")
    val hasProfile: Boolean,
    val thumbnail: ThumbnailDto?
)
