package ch.streckeisen.mycv.backend.coverletter

data class CoverLetterData(
    val language: String,
    val mirrorProfileImage: Boolean,
    val author: CoverLetterAuthor,
    val application: CoverLetterApplication,
    val documents: List<String>?
)

data class CoverLetterAuthor(
    val firstName: String,
    val lastName: String,
    val jobTitle: String,
    val email: String,
    val phone: String,
    val address: String
)

data class CoverLetterApplication(
    val jobTitle: String,
    val company: String,
    val contactPerson: CoverLetterContactPerson?,
    val addressee: String?,
    val salutation: String,
    val companyAddress: CoverLetterCompanyAddress,
    val content: String,
    val closing: String
)

data class CoverLetterCompanyAddress(
    val line1: String,
    val line2: String
)

data class CoverLetterContactPerson(
    val firstName: String,
    val lastName: String
)