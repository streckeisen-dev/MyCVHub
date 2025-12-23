package ch.streckeisen.mycv.backend.cv.generator

data class CVGenerationRequestDto(
    val includedWorkExperience: List<IncludedCVItem>?,
    val includedEducation: List<IncludedCVItem>?,
    val includedProjects: List<IncludedCVItem>?,
    val includedSkills: List<IncludedCVItem>?,
    val templateOptions: Map<String, String>?
)

data class IncludedCVItem(
    val id: Long,
    val includeDescription: Boolean = true
)