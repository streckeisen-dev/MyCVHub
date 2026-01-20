package ch.streckeisen.mycv.backend.coverletter

data class CoverLetterApplicationDto(
    val jobTitle: String?,
    val company: String?,
    val contactPerson: CoverLetterContactPersonDto?,
    val addressee: String?,
    val salutation: String?,
    val companyAddress: CoverLetterCompanyAddressDto?,
    val content: String?,
    val closing: String?
)

data class CoverLetterCompanyAddressDto(
    val street: String?,
    val postcode: String?,
    val city: String?
)

data class CoverLetterContactPersonDto(
    val firstName: String?,
    val lastName: String?
)