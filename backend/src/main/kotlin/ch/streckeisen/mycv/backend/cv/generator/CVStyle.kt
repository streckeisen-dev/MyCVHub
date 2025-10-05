package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX

enum class CVStyle(
    val cvTemplate: String,
    val nameKey: String,
    val descriptionKey: String,
    val options: List<CVStyleOption> = emptyList()
) {
    MODERN(
        "modern",
        "$MYCV_KEY_PREFIX.cv.style.modern.name",
        "$MYCV_KEY_PREFIX.cv.style.modern.description"
    ),
    TALENDO(
        "talendo",
        "$MYCV_KEY_PREFIX.cv.style.talendo.name",
        "$MYCV_KEY_PREFIX.cv.style.talendo.description",
        listOf(
            CVStyleOption("bannerBackground", "$MYCV_KEY_PREFIX.cv.style.talendo.bannerBackground", CVStyleOptionType.COLOR, "#000000")
        )
    );

    companion object {
        fun fromTemplate(template: String?): CVStyle? = entries.find { it.cvTemplate == template }
    }
}