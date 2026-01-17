package ch.streckeisen.mycv.backend.applicationTemplate

data class ApplicationTemplate(
    val id: Long,
    val name: String,
    val cvConfiguration: CvConfiguration,
    val documents: List<String>? = emptyList()
)
