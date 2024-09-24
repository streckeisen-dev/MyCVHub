package ch.streckeisen.mycv.backend.cv.skill

import ch.streckeisen.mycv.backend.cv.applicant.ApplicantRepository

fun Skill.toDto() = SkillDto(
    id,
    name,
    type,
    level,
    applicant.id!!
)

fun SkillDto.toEntity(applicantRepository: ApplicantRepository): Skill {
    val applicant = applicantRepository.findById(applicantId).orElseThrow()
    return Skill(
        id,
        name,
        type,
        level,
        applicant
    )
}