package ch.streckeisen.mycv.backend.cv.generator.data

import ch.streckeisen.mycv.backend.cv.generator.data.CVLink

data class CVEntry(
    val title: String,
    val location: String,
    val startDate: String,
    val endDate: String,
    val institution: String,
    val description: String?,
    val links: List<CVLink>
)