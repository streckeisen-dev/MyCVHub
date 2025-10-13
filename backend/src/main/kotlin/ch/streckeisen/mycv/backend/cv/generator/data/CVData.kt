package ch.streckeisen.mycv.backend.cv.generator.data

import ch.streckeisen.mycv.backend.cv.generator.data.CVEntry
import ch.streckeisen.mycv.backend.cv.generator.data.CVSkills

data class CVData(
    val language: String,
    val firstName: String,
    val lastName: String,
    val jobTitle: String,
    val bio: String?,
    val email: String,
    val phone: String,
    val address: String,
    val birthday: String,
    val workExperiences: List<CVEntry>,
    val skills: List<CVSkills>,
    val education: List<CVEntry>,
    val projects: List<CVEntry>,
    val templateOptions: Map<String, String>
)