package ch.streckeisen.mycv.backend.cv.generator.data

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.cv.education.EducationEntity
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceEntity
import ch.streckeisen.mycv.backend.cv.generator.IncludedCVItem
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.project.ProjectEntity
import ch.streckeisen.mycv.backend.cv.skill.SkillEntity
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val BIRTHDAY_FORMAT = "dd.MM.yyyy"
private const val CV_DATE_FORMAT = "MM.yyyy"

private const val TODAY_MESSAGE = "$MYCV_KEY_PREFIX.date.today"

@Service
class CVDataService(
    val messagesService: MessagesService
) {
    fun prepareWorkExperiences(
        workExperiences: List<WorkExperienceEntity>,
        includedItems: List<IncludedCVItem>? = null
    ): List<WorkExperienceEntity> = workExperiences.filter { w ->
        includedItems == null || includedItems.any { incl -> incl.id == w.id }
    }.map { w ->
        val includedExperience = includedItems?.find { incl -> incl.id == w.id }
        if (includedItems != null && includedExperience != null && !includedExperience.includeDescription) {
            WorkExperienceEntity(
                w.id,
                w.jobTitle,
                w.company,
                w.positionStart,
                w.positionEnd,
                w.location,
                "",
                w.profile
            )
        } else w
    }

    fun prepareEducation(
        education: List<EducationEntity>,
        includedItems: List<IncludedCVItem>?
    ): List<EducationEntity> = education.filter { e ->
        includedItems == null || includedItems.any { incl -> incl.id == e.id }
    }.map { e ->
        val includedEducation = includedItems?.find { incl -> incl.id == e.id }
        if (includedItems != null && includedEducation != null && !includedEducation.includeDescription) {
            EducationEntity(
                e.id,
                e.institution,
                e.location,
                e.educationStart,
                e.educationEnd,
                e.degreeName,
                null,
                e.profile
            )
        } else e
    }


    fun prepareProjects(
        projects: List<ProjectEntity>,
        includedItems: List<IncludedCVItem>?
    ): List<ProjectEntity> = projects.filter { p ->
        includedItems == null || includedItems.any { incl -> incl.id == p.id }
    }.map { p ->
        val includedProject = includedItems?.find { incl -> incl.id == p.id }
        if (includedItems != null && includedProject != null && !includedProject.includeDescription) {
            ProjectEntity(
                p.id,
                p.name,
                p.role,
                "",
                p.projectStart,
                p.projectEnd,
                p.links,
                p.profile
            )
        } else p
    }

    fun prepareSkills(
        skills: List<SkillEntity>,
        includedItems: List<IncludedCVItem>?
    ): List<SkillEntity> = skills.filter { s ->
        includedItems == null || includedItems.any { incl -> incl.id == s.id }
    }

    fun createCVData(
        locale: Locale,
        profile: ProfileEntity,
        workExperience: List<WorkExperienceEntity>,
        education: List<EducationEntity>,
        projects: List<ProjectEntity>,
        skills: List<SkillEntity>,
        templateOptions: Map<String, String>
    ): CVData {
        val cvDateFormatter = DateTimeFormatter.ofPattern(CV_DATE_FORMAT, locale)
        return CVData(
            language = locale.language,
            firstName = profile.account.accountDetails!!.firstName,
            lastName = profile.account.accountDetails.lastName,
            jobTitle = profile.jobTitle,
            bio = profile.bio,
            email = profile.account.accountDetails.email,
            phone = profile.account.accountDetails.phone,
            address = getAddressString(profile.account.accountDetails),
            birthday = getBirthday(profile.account.accountDetails.birthday),
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
            templateOptions
        )
    }

    private fun getAddressString(account: AccountDetailsEntity): String {
        val addressBuilder = StringBuilder()
        addressBuilder.append(account.street)
        if (account.houseNumber != null) {
            addressBuilder.append(" ${account.houseNumber}")
        }
        addressBuilder.append(", ${account.postcode} ${account.city}")
        return addressBuilder.toString()
    }

    private fun getBirthday(birthday: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern(BIRTHDAY_FORMAT)
        return birthday.format(formatter)
    }
}