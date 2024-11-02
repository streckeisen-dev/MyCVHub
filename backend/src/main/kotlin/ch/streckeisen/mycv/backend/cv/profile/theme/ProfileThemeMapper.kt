package ch.streckeisen.mycv.backend.cv.profile.theme

fun ProfileThemeEntity.toDto() = ProfileThemeDto(
    backgroundColor,
    surfaceColor
)