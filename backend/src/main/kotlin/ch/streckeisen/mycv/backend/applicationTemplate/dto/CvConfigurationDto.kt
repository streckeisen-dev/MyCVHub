package ch.streckeisen.mycv.backend.applicationTemplate.dto

data class CvConfigurationDto(
    val cvStyle: String,
    val includedCvContent: IncludedCvContentDto?,
    val cvStyleOptions: Map<String, String>?
)

data class IncludedCvContentDto(
    val includedWorkExperience: List<CvEntrySelectionDto>,
    val includedEducation: List<CvEntrySelectionDto>,
    val includedProjects: List<CvEntrySelectionDto>,
    val includedSkills: List<Long>,
)

data class CvEntrySelectionDto(
    val id: Long,
    val includeDescription: Boolean
)
