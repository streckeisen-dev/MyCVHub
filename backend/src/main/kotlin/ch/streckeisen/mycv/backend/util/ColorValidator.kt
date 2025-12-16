package ch.streckeisen.mycv.backend.util

private val colorRegex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$".toRegex()

fun isValidHexColor(color: String): Boolean = colorRegex.matches(color)