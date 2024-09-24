package ch.streckeisen.mycv.backend.exceptions

class ResultNotFoundException(
    message: String,
    cause: Throwable? = null
): Exception(message, cause)