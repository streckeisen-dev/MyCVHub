package ch.streckeisen.mycv.backend.cv.generator

data class CVStyleOption(
    val key: String,
    val nameKey: String,
    val type: CVStyleOptionType,
    val defaultValue: String
)

enum class CVStyleOptionType {
    COLOR,
    STRING
}