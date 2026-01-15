package ch.streckeisen.mycv.backend.coverletter

data class CoverLetterGenerationRequestDto(
    val language: String?,
    val style: String?,
    val mirrorProfileImage: Boolean?,
    val application: CoverLetterApplicationDto?,
    val attachedDocuments: List<String?>?
)
