package ch.streckeisen.mycv.backend.exceptions

class LocalizedException(val messageKey: String, vararg val args: String) : Exception()