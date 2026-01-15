package ch.streckeisen.mycv.backend.coverletter

data class CoverLetterApplicationDto(
    val jobTitle: String?,
    val company: String?,
    val contactPerson: String?,
    val addressee: String?,
    val salutation: String?,
    val companyAddress: CoverLetterCompanyAddressDto?,
    val content: String?
)

data class CoverLetterCompanyAddressDto(
    val street: String?,
    val zipCode: String?,
    val city: String?
)