package ch.streckeisen.mycv.backend.cv.generator

data class CVProfile(
    val language: String,
    val firstName: String,
    val lastName: String,
    val jobTitle: String,
    val bio: String?,
    val email: String,
    val phone: String,
    val address: String,
    val birthday: String,
    val workExperiences: List<CVResumeEntry>,
    val skills: List<CVSkills>,
    val education: List<CVResumeEntry>,
    val projects: List<CVResumeEntry>
)
