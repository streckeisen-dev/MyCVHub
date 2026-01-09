package ch.streckeisen.mycv.backend.applicationTemplate

data class ApplicationTemplateDto(
    val id: Long,
    val name: String,
    val cvConfiguration: CvConfigurationDto,
    val documentChecklist: List<String>?
)
