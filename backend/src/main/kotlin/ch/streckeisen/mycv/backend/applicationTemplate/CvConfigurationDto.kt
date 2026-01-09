package ch.streckeisen.mycv.backend.applicationTemplate

data class CvConfigurationDto(
    val includedWorkExperience: List<CvEntrySelectionDto>?,
    val includedEducation: List<CvEntrySelectionDto>?,
    val includedProjects: List<CvEntrySelectionDto>?,
    val includedSkills: List<Long>?,

    val cvTemplate: String,
    val templateParameters: Map<String, String>
)

data class CvEntrySelectionDto(
    val entityId: Long,
    val includeDescription: Boolean
)
