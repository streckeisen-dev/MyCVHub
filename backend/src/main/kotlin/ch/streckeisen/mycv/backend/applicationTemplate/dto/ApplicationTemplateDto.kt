package ch.streckeisen.mycv.backend.applicationTemplate.dto

data class ApplicationTemplateDto(
    val id: Long,
    val name: String,
    val cvConfiguration: CvConfigurationDto,
    val documentChecklist: List<String>?
)
