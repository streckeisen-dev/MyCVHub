package ch.streckeisen.mycv.backend.applicationTemplate

data class ApplicationTemplate(
    val id: Long,
    val name: String,
    val cvConfiguration: CvConfiguration,
    val coverLetterConfiguration: CoverLetterConfiguration
)
