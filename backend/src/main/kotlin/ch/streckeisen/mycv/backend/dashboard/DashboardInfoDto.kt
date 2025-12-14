package ch.streckeisen.mycv.backend.dashboard

import com.fasterxml.jackson.annotation.JsonProperty

data class DashboardInfoDto(
    @get:JsonProperty(value = "isVerified")
    val isVerified: Boolean,
    val profile: ProfileInfoDto?
)

data class ProfileInfoDto(
    val experienceCount: Int,
    val educationCount: Int,
    val projectCount: Int,
    val skillCount: Int
)
