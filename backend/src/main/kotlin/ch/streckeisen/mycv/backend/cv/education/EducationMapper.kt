package ch.streckeisen.mycv.backend.cv.education

fun EducationEntity.toDto() = EducationDto(
    id!!,
    institution,
    location,
    educationStart,
    educationEnd,
    degreeName,
    description
)