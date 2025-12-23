package ch.streckeisen.mycv.backend.dashboard

import com.fasterxml.jackson.annotation.JsonProperty

data class DashboardInfoDto(
    @get:JsonProperty(value = "isVerified")
    val isVerified: Boolean,
    val profile: ProfileInfoDto?,
    val applications: List<ApplicationInfoDto>
)

