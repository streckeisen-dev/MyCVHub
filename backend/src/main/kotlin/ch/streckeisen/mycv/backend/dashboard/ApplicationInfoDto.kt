package ch.streckeisen.mycv.backend.dashboard

import ch.streckeisen.mycv.backend.application.dto.ApplicationStatusDto

data class ApplicationInfoDto(
    val status: ApplicationStatusDto,
    val count: Long
)
