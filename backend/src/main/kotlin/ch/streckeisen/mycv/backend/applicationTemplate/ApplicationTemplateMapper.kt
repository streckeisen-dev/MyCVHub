package ch.streckeisen.mycv.backend.applicationTemplate

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

fun ApplicationTemplate.toDto() = ApplicationTemplateDto(id, name, cvConfiguration.toDto(), documentChecklist)

fun CvConfiguration.toDto() = CvConfigurationDto(
    includedWorkExperience?.toDto(),
    includedEducation?.toDto(),
    includedProjects?.toDto(),
    includedSkills,
    cvTemplate,
    templateParameters
)

fun List<CvEntrySelection>.toDto() = map { it.toDto() }

fun CvEntrySelection.toDto() = CvEntrySelectionDto(entityId, includeDescription)

fun ApplicationTemplateEntity.toFullObject(objectMapper: ObjectMapper) = ApplicationTemplate(
    id = id!!,
    name = name,
    cvConfiguration = objectMapper.readValue(cvConfiguration, CvConfiguration::class.java),
    documentChecklist = objectMapper.readValue(
        documentChecklist,
        object : TypeReference<List<String>>() {}
    )
)