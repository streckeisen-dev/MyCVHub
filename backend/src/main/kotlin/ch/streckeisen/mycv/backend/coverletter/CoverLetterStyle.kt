package ch.streckeisen.mycv.backend.coverletter

import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX

enum class CoverLetterStyle(
    val styleKey: String,
    val nameKey: String,
    val descriptionKey: String
) {
    MODERN(
        "modern",
        "$MYCV_KEY_PREFIX.coverletter.style.modern.name",
        "$MYCV_KEY_PREFIX.coverletter.style.modern.description"
    );

    companion object {
        fun fromStyleKey(styleKey: String?): CoverLetterStyle? = entries.find { it.styleKey == styleKey }
    }
}