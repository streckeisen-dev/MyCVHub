package ch.streckeisen.mycv.backend.applicationTemplate

data class CvConfigurationUpdateDto(
    val includedWorkExperience: List<CvEntrySelectionUpdateDto>?,
    val includedEducation: List<CvEntrySelectionUpdateDto>?,
    val includedProjects: List<CvEntrySelectionUpdateDto>?,
    val includedSkills: List<Long>?,

    val cvTemplate: String?,
    val templateOptions: Map<String, String>?
) {
    fun toCvConfiguration(): CvConfiguration {
        return CvConfiguration(
            includedWorkExperience?.map { it.toCvConfiguration() },
            includedEducation?.map { it.toCvConfiguration() },
            includedProjects?.map { it.toCvConfiguration() },
            includedSkills,
            cvTemplate!!,
            templateOptions!!
        )
    }
}

data class CvEntrySelectionUpdateDto(
    val entityId: Long?,
    val includeDescription: Boolean?
) {
    fun toCvConfiguration(): CvEntrySelection = CvEntrySelection(entityId!!, includeDescription ?: true)
}
