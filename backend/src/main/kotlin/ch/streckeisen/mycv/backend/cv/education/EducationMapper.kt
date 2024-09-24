package ch.streckeisen.mycv.backend.cv.education

import ch.streckeisen.mycv.backend.cv.applicant.ApplicantRepository

fun Education.toDto() = EducationDto(
    id,
    school,
    location,
    educationStart,
    educationEnd,
    degreeName,
    description,
    applicant.id!!
)

fun EducationDto.toEntity(applicantRepository: ApplicantRepository) {
    val applicant = applicantRepository.findById(applicantId).orElseThrow()
    Education(
        id,
        school,
        location,
        educationStart,
        educationEnd,
        degreeName,
        description,
        applicant
    )
}