package ch.streckeisen.mycv.backend.coverletter

data class CoverLetterGenerationSnapshot(
    val accountId: Long,
    val isVerified: Boolean,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?,
    val street: String?,
    val houseNumber: String?,
    val postcode: String?,
    val city: String?,
    val profilePicture: String?,
    val jobTitle: String?
)
{
    fun isIncomplete(): Boolean = !isVerified
        || firstName == null
        || lastName == null
        || email == null
        || phone == null
        || street == null
        || postcode == null
        || city == null
        || profilePicture == null
        || jobTitle == null
}