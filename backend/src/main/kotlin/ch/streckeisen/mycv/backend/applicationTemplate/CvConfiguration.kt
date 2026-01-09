package ch.streckeisen.mycv.backend.applicationTemplate

data class CvConfiguration(
    val includedWorkExperience: List<CvEntrySelection>? = emptyList(),
    val includedEducation: List<CvEntrySelection>? = emptyList(),
    val includedProjects: List<CvEntrySelection>? = emptyList(),
    val includedSkills: List<Long>? = emptyList(),

    val cvTemplate: String,
    val templateParameters: Map<String, String> = emptyMap()
)

data class CvEntrySelection(
    val entityId: Long,
    val includeDescription: Boolean = true
)
