package ch.streckeisen.mycv.backend.cv.generator

data class CVStyleDto(
    val key: String,
    val name: String,
    val description: String,
    val options: List<CVStyleOptionDto> = emptyList()
)

data class CVStyleOptionDto(
    val key: String,
    val name: String,
    val type: CVStyleOptionType,
    val default: String
)
