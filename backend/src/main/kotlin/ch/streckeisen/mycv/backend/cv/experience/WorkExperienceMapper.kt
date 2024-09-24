package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.cv.applicant.ApplicantRepository

fun WorkExperience.toDto() = WorkExperienceDto(
    id,
    company,
    positionStart,
    positionEnd,
    location,
    description,
    applicant.id!!
)

fun WorkExperienceDto.toEntity(applicantRepository: ApplicantRepository): WorkExperience {
    val applicant = applicantRepository.findById(applicantId).orElseThrow()
    return WorkExperience(
        id,
        company,
        positionStart,
        positionEnd,
        location,
        description,
        applicant
    )
}