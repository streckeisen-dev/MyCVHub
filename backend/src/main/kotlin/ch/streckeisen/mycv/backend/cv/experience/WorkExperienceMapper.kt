package ch.streckeisen.mycv.backend.cv.experience

fun WorkExperienceEntity.toDto() = WorkExperienceDto(
    id!!,
    jobTitle,
    company,
    positionStart,
    positionEnd,
    location,
    description
)