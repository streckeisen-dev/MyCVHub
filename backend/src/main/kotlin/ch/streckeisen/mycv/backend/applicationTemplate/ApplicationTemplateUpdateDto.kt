package ch.streckeisen.mycv.backend.applicationTemplate

data class ApplicationTemplateUpdateDto(
    val id: Long?,
    val name: String?,
    val cvConfiguration: CvConfigurationUpdateDto?,
    val documentChecklist: List<String>?
)
