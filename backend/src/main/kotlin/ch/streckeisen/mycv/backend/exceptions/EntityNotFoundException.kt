package ch.streckeisen.mycv.backend.exceptions

class EntityNotFoundException(
    message: String,
    cause: Throwable? = null
): Exception(message, cause)