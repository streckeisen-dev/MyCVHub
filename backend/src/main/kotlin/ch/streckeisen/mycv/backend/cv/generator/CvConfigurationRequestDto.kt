package ch.streckeisen.mycv.backend.cv.generator

data class CvConfigurationRequestDto(
    val cvStyle: String?,
    val includedCvContent: IncludedCvContentDto?,
    val cvStyleOptions: Map<String, String>?
)

data class IncludedCvContentDto(
    val includedWorkExperience: List<IncludedCVItem>?,
    val includedEducation: List<IncludedCVItem>?,
    val includedProjects: List<IncludedCVItem>?,
    val includedSkills: List<Long>?,
)

data class IncludedCVItem(
    val id: Long?,
    val includeDescription: Boolean? = true
)