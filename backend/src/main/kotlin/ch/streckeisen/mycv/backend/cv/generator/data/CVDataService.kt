package ch.streckeisen.mycv.backend.cv.generator.data

import ch.streckeisen.mycv.backend.cv.generator.CVEducationSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVGenerationSnapshot
import ch.streckeisen.mycv.backend.cv.generator.IncludedCVItem
import ch.streckeisen.mycv.backend.cv.generator.CVProjectSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVSkillSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVWorkExperienceSnapshot
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val BIRTHDAY_FORMAT = "dd.MM.yyyy"
private const val CV_DATE_FORMAT = "MM.yyyy"

private const val TODAY_MESSAGE = "$MYCV_KEY_PREFIX.date.today"

@Service
class CVDataService(
    val messagesService: MessagesService
) {
    fun filterWorkExperiences(
        workExperiences: List<CVWorkExperienceSnapshot>,
        includedItems: List<IncludedCVItem>? = null
    ): List<CVWorkExperienceSnapshot> = workExperiences.filter { w ->
        includedItems == null || includedItems.any { incl -> incl.id == w.id }
    }.map { w ->
        val includedExperience = includedItems?.find { incl -> incl.id == w.id }
        if (includedItems != null && includedExperience != null && !(includedExperience.includeDescription ?: true)) {
            w.copy(description = "")
        } else w
    }

    fun filterEducation(
        education: List<CVEducationSnapshot>,
        includedItems: List<IncludedCVItem>?
    ): List<CVEducationSnapshot> = education.filter { e ->
        includedItems == null || includedItems.any { incl -> incl.id == e.id }
    }.map { e ->
        val includedEducation = includedItems?.find { incl -> incl.id == e.id }
        if (includedItems != null && includedEducation != null && !(includedEducation.includeDescription ?: true)) {
            e.copy(description = null)
        } else e
    }


    fun filterProjects(
        projects: List<CVProjectSnapshot>,
        includedItems: List<IncludedCVItem>?
    ): List<CVProjectSnapshot> = projects.filter { p ->
        includedItems == null || includedItems.any { incl -> incl.id == p.id }
    }.map { p ->
        val includedProject = includedItems?.find { incl -> incl.id == p.id }
        if (includedItems != null && includedProject != null && !(includedProject.includeDescription ?: true)) {
            p.copy(description = "")
        } else p
    }

    fun filterSkills(
        skills: List<CVSkillSnapshot>,
        includedItems: List<Long>?
    ): List<CVSkillSnapshot> = skills.filter { s ->
        includedItems == null || includedItems.any { skillId -> skillId == s.id }
    }

    fun createCVData(
        profile: CVGenerationSnapshot,
        workExperience: List<CVWorkExperienceSnapshot>,
        education: List<CVEducationSnapshot>,
        projects: List<CVProjectSnapshot>,
        skills: List<CVSkillSnapshot>,
        cvStyleOptions: Map<String, String>
    ): CVData {
        val locale = LocaleContextHolder.getLocale()
        val cvDateFormatter = DateTimeFormatter.ofPattern(CV_DATE_FORMAT, locale)
        return CVData(
            language = locale.language,
            firstName = profile.firstName!!,
            lastName = profile.lastName!!,
            jobTitle = profile.jobTitle,
            bio = profile.bio,
            email = profile.email!!,
            phone = profile.phone!!,
            address = getAddressString(profile),
            birthday = getBirthday(profile.birthday!!),
            workExperiences = workExperience.map {
                CVEntry(
                    title = it.jobTitle,
                    location = it.location,
                    startDate = it.positionStart.format(cvDateFormatter),
                    endDate = it.positionEnd?.format(cvDateFormatter)
                        ?: messagesService.getMessage(TODAY_MESSAGE),
                    institution = it.company,
                    it.description,
                    links = listOf()
                )
            },
            skills = skills.groupBy { it.type }.entries
                .map { entry ->
                    CVSkills(
                        entry.key,
                        entry.value
                            .sortedByDescending { s -> s.level }
                            .map { s -> s.name }
                    )
                },
            education = education.map {
                CVEntry(
                    title = it.degreeName,
                    location = it.location,
                    startDate = it.educationStart.format(cvDateFormatter),
                    endDate = it.educationEnd?.format(cvDateFormatter)
                        ?: messagesService.getMessage(TODAY_MESSAGE),
                    institution = it.institution,
                    description = it.description,
                    links = listOf()
                )
            },
            projects = projects.map {
                CVEntry(
                    title = it.name,
                    location = "",
                    startDate = it.projectStart.format(cvDateFormatter),
                    endDate = it.projectEnd?.format(cvDateFormatter)
                        ?: messagesService.getMessage(TODAY_MESSAGE),
                    institution = it.role,
                    description = it.description,
                    links = it.links.map { link ->
                        CVLink(
                            link.url,
                            link.displayName,
                            link.type.name
                        )
                    }
                )
            },
            cvStyleOptions
        )
    }

    private fun getAddressString(profile: CVGenerationSnapshot): String {
        val addressBuilder = StringBuilder()
        addressBuilder.append(profile.street)
        if (profile.houseNumber != null) {
            addressBuilder.append(" ${profile.houseNumber}")
        }
        addressBuilder.append(", ${profile.postcode} ${profile.city}")
        return addressBuilder.toString()
    }

    private fun getBirthday(birthday: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern(BIRTHDAY_FORMAT)
        return birthday.format(formatter)
    }
}
