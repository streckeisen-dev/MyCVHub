package ch.streckeisen.mycv.backend.applicationTemplate.dto

import ch.streckeisen.mycv.backend.cv.generator.CvConfigurationRequestDto

data class ApplicationTemplateUpdateDto(
    val id: Long?,
    val name: String?,
    val cvConfiguration: CvConfigurationRequestDto?,
    val coverLetterConfiguration: CoverLetterConfigurationUpdateDto?
)
