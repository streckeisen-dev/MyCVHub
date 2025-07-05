package ch.streckeisen.mycv.backend.cv.generator

data class CVResumeEntry(
    val title: String,
    val location: String,
    val startDate: String,
    val endDate: String,
    val institution: String,
    val description: String?,
    val links: List<CVLink>
)
