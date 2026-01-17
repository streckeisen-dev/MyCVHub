package ch.streckeisen.mycv.backend.applicationTemplate

import ch.streckeisen.mycv.backend.applicationTemplate.dto.ApplicationTemplateDto
import ch.streckeisen.mycv.backend.applicationTemplate.dto.CvConfigurationDto
import ch.streckeisen.mycv.backend.applicationTemplate.dto.CvEntrySelectionDto
import ch.streckeisen.mycv.backend.cv.generator.CvConfigurationRequestDto
import ch.streckeisen.mycv.backend.cv.generator.IncludedCVItem
import ch.streckeisen.mycv.backend.cv.generator.IncludedCvContentDto
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper


fun ApplicationTemplate.toDto() = ApplicationTemplateDto(id, name, cvConfiguration.toDto(), documents)

fun CvConfiguration.toDto() = CvConfigurationDto(
    cvStyle = cvStyle,
    includedCvContent = includedCvContent?.toDto(),
    cvStyleOptions = cvStyleOptions
)

fun IncludedCvContent.toDto() = ch.streckeisen.mycv.backend.applicationTemplate.dto.IncludedCvContentDto(
    includedWorkExperience.toDto(),
    includedEducation.toDto(),
    includedProjects.toDto(),
    includedSkills
)

fun List<CvEntrySelection>.toDto() = map { it.toDto() }

fun CvEntrySelection.toDto() = CvEntrySelectionDto(entityId, includeDescription)

fun ApplicationTemplateEntity.toFullObject(objectMapper: ObjectMapper) = ApplicationTemplate(
    id = id!!,
    name = name,
    cvConfiguration = objectMapper.readValue(cvConfiguration, CvConfiguration::class.java),
    documents = if (documents == null) null else objectMapper.readValue(
        documents,
        object : TypeReference<List<String>>() {}
    )
)

fun CvConfigurationRequestDto.toCvConfiguration(): CvConfiguration = CvConfiguration(
    cvStyle = cvStyle!!,
    includedCvContent = includedCvContent?.toCvConfiguration(),
    cvStyleOptions = cvStyleOptions
)

fun IncludedCvContentDto.toCvConfiguration() = IncludedCvContent(
    includedWorkExperience = includedWorkExperience!!.map { it.toCvConfiguration() },
    includedEducation = includedEducation!!.map { it.toCvConfiguration() },
    includedProjects = includedProjects!!.map { it.toCvConfiguration() },
    includedSkills = includedSkills!!
)

fun IncludedCVItem.toCvConfiguration() = CvEntrySelection(
    entityId = id!!,
    includeDescription ?: true
)