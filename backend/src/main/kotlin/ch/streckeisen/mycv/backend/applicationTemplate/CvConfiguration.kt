package ch.streckeisen.mycv.backend.applicationTemplate

data class CvConfiguration(
    val includedCvContent: IncludedCvContent?,
    val cvStyle: String,
    val cvStyleOptions: Map<String, String>?
)

data class IncludedCvContent(
    val includedWorkExperience: List<CvEntrySelection>,
    val includedEducation: List<CvEntrySelection>,
    val includedProjects: List<CvEntrySelection>,
    val includedSkills: List<Long>,
)

data class CvEntrySelection(
    val entityId: Long,
    val includeDescription: Boolean = true
)
