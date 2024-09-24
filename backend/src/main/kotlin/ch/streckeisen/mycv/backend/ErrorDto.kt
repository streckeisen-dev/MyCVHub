package ch.streckeisen.mycv.backend

data class ErrorDto(
    val message: String,
    val errors: Map<String, String>? = emptyMap(),
)
