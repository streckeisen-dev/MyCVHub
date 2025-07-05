package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private const val BIRTHDAY_FORMAT = "dd.MM.yyyy"
private const val CV_DATE_FORMAT = "MMMM yyyy"

private const val TODAY_MESSAGE = "$MYCV_KEY_PREFIX.date.today"

fun ProfileEntity.toCVProfile(locale: Locale, messagesService: MessagesService): CVProfile {
    val cvDateFormatter = DateTimeFormatter.ofPattern(CV_DATE_FORMAT, locale)
    return CVProfile(
        language = locale.language,
        firstName = this.account.accountDetails!!.firstName,
        lastName = this.account.accountDetails.lastName,
        jobTitle = this.jobTitle,
        bio = this.bio,
        email = this.account.accountDetails.email,
        phone = this.account.accountDetails.phone,
        address = getAddressString(this.account.accountDetails),
        birthday = getBirthday(this.account.accountDetails.birthday),
        workExperiences = this.workExperiences.map {
            CVResumeEntry(
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
        skills = this.skills.groupBy { it.type }.entries
            .map { entry ->
                CVSkills(entry.key, entry.value.map { it.name })
            },
        education = this.education.map {
            CVResumeEntry(
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
        projects = this.projects.map {
            CVResumeEntry(
                title = it.name,
                location = "",
                startDate = it.projectStart.format(cvDateFormatter),
                endDate = it.projectEnd?.format(cvDateFormatter)
                    ?: messagesService.getMessage(TODAY_MESSAGE),
                institution = it.role,
                description = it.description,
                links = it.links.map { link -> CVLink(link.url, link.displayName, link.type.name) }
            )
        }
    )
}

fun getAddressString(account: AccountDetailsEntity): String {
    val addressBuilder = StringBuilder()
    addressBuilder.append(account.street)
    if (account.houseNumber != null) {
        addressBuilder.append(" ${account.houseNumber}")
    }
    addressBuilder.append(", ${account.postcode} ${account.city}")
    return addressBuilder.toString()
}

fun getBirthday(birthday: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern(BIRTHDAY_FORMAT)
    return birthday.format(formatter)
}