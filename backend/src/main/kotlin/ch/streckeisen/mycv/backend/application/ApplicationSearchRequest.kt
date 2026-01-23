package ch.streckeisen.mycv.backend.application

data class ApplicationSearchRequest(
    val page: Int,
    val pageSize: Int,
    val searchTerm: String?,
    val status: ApplicationStatus?,
    val includeArchived: Boolean,
    val sort: String?,
    val sortDirection: String?
)
