package ch.streckeisen.mycv.backend.cv.generator.data

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.cv.education.EducationEntity
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceEntity
import ch.streckeisen.mycv.backend.cv.generator.IncludedCVItem
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.project.ProjectEntity
import ch.streckeisen.mycv.backend.cv.project.ProjectLink
import ch.streckeisen.mycv.backend.cv.project.ProjectLinkType
import ch.streckeisen.mycv.backend.cv.skill.SkillEntity
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.time.LocalDate

private const val CURRENT_JOB_TITLE = "Current Job"
private const val TECH_COMPANY = "Tech Inc."
private const val TODAY = "Today"
private const val FRAMEWORK_SKILLS = "Framework"
private const val TECHNOLOGY_SKILLS = "Technology"
private const val LANGUAGE_SKILLS = "Language"
private const val CLOUD_PLATFORM_SKILLS = "Cloud Platform"

class CVDataServiceTest {
    private lateinit var messagesService: MessagesService
    private lateinit var cvDataService: CVDataService

    @BeforeEach
    fun setup() {
        messagesService = mockk(relaxed = true) {
            every { getMessage(eq("$MYCV_KEY_PREFIX.date.today")) } returns TODAY
        }
        cvDataService = CVDataService(messagesService)
    }

    @Test
    fun testFilterWorkExperienceWithoutFilter() {
        val experiences = workExperiences()

        val result = cvDataService.filterWorkExperiences(experiences, null)

        assertEquals(experiences.size, result.size)
        assertEquals(experiences, result)
    }

    @Test
    fun testFilterWorkExperienceWithIdFilter() {
        val experiences = workExperiences()
        val includedItems = listOf(IncludedCVItem(1, true))

        val result = cvDataService.filterWorkExperiences(experiences, includedItems)

        assertEquals(includedItems.size, result.size)
        assertEquals(listOf(experiences[0]), result)
    }

    @Test
    fun testFilterWorkExperienceWithIdFilterAndNoResults() {
        val experiences = workExperiences()
        val includedItems = listOf<IncludedCVItem>()

        val result = cvDataService.filterWorkExperiences(experiences, includedItems)

        assertEquals(0, result.size)
    }

    @Test
    fun testFilterWorkExperienceWithDescriptionFilter() {
        val experiences = workExperiences()
        val includedItems = listOf(IncludedCVItem(1, false), IncludedCVItem(2, true))

        val result = cvDataService.filterWorkExperiences(experiences, includedItems)

        assertEquals(experiences.size, result.size)

        for (i in 0 until experiences.size) {
            val experience = experiences[i]
            val preparedExperience = result[i]
            val incl = includedItems[i]

            if (incl.includeDescription) {
                assertEquals(experience, preparedExperience)
            } else {
                assertEquals(experience.id, preparedExperience.id)
                assertEquals("", preparedExperience.description)
                assertEquals(experience.location, preparedExperience.location)
                assertEquals(experience.profile, preparedExperience.profile)
                assertEquals(experience.company, preparedExperience.company)
                assertEquals(experience.positionStart, preparedExperience.positionStart)
                assertEquals(experience.positionEnd, preparedExperience.positionEnd)
                assertEquals(experience.jobTitle, preparedExperience.jobTitle)
            }
        }
    }

    @Test
    fun testFilterEducationWithoutFilter() {
        val education = education()

        val result = cvDataService.filterEducation(education, null)

        assertEquals(education.size, result.size)
        assertEquals(education, result)
    }

    @Test
    fun testFilterEducationWithIdFilter() {
        val education = education()
        val includedItems = listOf(IncludedCVItem(1, true))

        val result = cvDataService.filterEducation(education, includedItems)

        assertEquals(includedItems.size, result.size)
        assertEquals(listOf(education[0]), result)
    }

    @Test
    fun testFilterEducationWithIdFilterAndNoResults() {
        val education = education()

        val result = cvDataService.filterEducation(education, listOf())

        assertEquals(0, result.size)
    }

    @Test
    fun testFilterEducationWithDescriptionFilter() {
        val education = education()
        val includedItems = listOf(IncludedCVItem(1, false))

        val result = cvDataService.filterEducation(education, includedItems)

        assertEquals(education.size, result.size)

        for (i in 0 until education.size) {
            val educationEntry = education[i]
            val preparedEntry = result[i]
            val incl = includedItems[i]

            if (incl.includeDescription) {
                assertEquals(educationEntry, preparedEntry)
            } else {
                assertEquals(educationEntry.id, preparedEntry.id)
                assertNull(preparedEntry.description)
                assertEquals(educationEntry.institution, preparedEntry.institution)
                assertEquals(educationEntry.location, preparedEntry.location)
                assertEquals(educationEntry.educationStart, preparedEntry.educationStart)
                assertEquals(educationEntry.educationEnd, preparedEntry.educationEnd)
                assertEquals(educationEntry.degreeName, preparedEntry.degreeName)
            }
        }
    }

    @Test
    fun testFilterProjectsWithoutFilter() {
        val projects = projects()

        val result = cvDataService.filterProjects(projects, null)

        assertEquals(projects.size, result.size)
        assertEquals(projects, result)
    }

    @Test
    fun testFilterProjectsWithIdFilter() {
        val projects = projects()
        val includedItems = listOf(IncludedCVItem(1, true))

        val result = cvDataService.filterProjects(projects, includedItems)

        assertEquals(includedItems.size, result.size)
        assertEquals(listOf(projects[0]), result)
    }

    @Test
    fun testFilterProjectsWithIdFilterAndNoResults() {
        val projects = projects()

        val result = cvDataService.filterProjects(projects, listOf())

        assertEquals(0, result.size)
    }

    @Test
    fun testFilterProjectsWithDescriptionFilter() {
        val projects = projects()
        val includedItems = listOf(IncludedCVItem(1, false), IncludedCVItem(2, true))

        val result = cvDataService.filterProjects(projects, includedItems)

        assertEquals(projects.size, result.size)

        for (i in 0 until projects.size) {
            val project = projects[i]
            val preparedProject = result[i]
            val incl = includedItems[i]

            if (incl.includeDescription) {
                assertEquals(project, preparedProject)
            } else {
                assertEquals(project.id, preparedProject.id)
                assertEquals("", preparedProject.description)
                assertEquals(project.projectStart, preparedProject.projectStart)
                assertEquals(project.projectEnd, preparedProject.projectEnd)
                assertEquals(project.profile, preparedProject.profile)
                assertEquals(project.name, preparedProject.name)
                assertEquals(project.role, preparedProject.role)
                assertEquals(project.links, preparedProject.links)
            }
        }
    }

    @Test
    fun testFilterSkillsWithoutFilter() {
        val skills = skills()

        val result = cvDataService.filterSkills(skills, null)

        assertEquals(skills.size, result.size)
        assertEquals(skills, result)
    }

    @Test
    fun testFilterSkillsWithIdFilter() {
        val skills = skills()
        val includedItems = listOf(IncludedCVItem(1, true), IncludedCVItem(3, true))

        val result = cvDataService.filterSkills(skills, includedItems)

        assertEquals(includedItems.size, result.size)
        assertEquals(listOf(skills[0], skills[2]), result)
    }

    @Test
    fun testFilterSkillsWithIdFilterAndNoResults() {
        val skills = skills()

        val result = cvDataService.filterSkills(skills, listOf())

        assertEquals(0, result.size)
    }

    @Test
    fun testFilterSkillsWithDescriptionFilter() {
        val skills = skills()
        val includedItems = listOf(IncludedCVItem(1, false), IncludedCVItem(2, true))

        val result = cvDataService.filterSkills(skills, includedItems)

        assertEquals(includedItems.size, result.size)
        assertEquals(listOf(skills[0], skills[1]), result)
    }

    @Test
    fun testCreateCVData() {
        val profile = profile()

        val cvData = cvDataService.createCVData(
            profile,
            workExperiences(),
            education(),
            projects(),
            skills(),
            mapOf("test" to "value")
        )

        val expectedCvData = fullCVData()
        assertEquals(expectedCvData, cvData)
    }

    @Test
    fun testCreateCVDataWithHiddenDescription() {
        val workExperiences = cvDataService.filterWorkExperiences(workExperiences(), listOf(IncludedCVItem(1, false)))

        val cvData = cvDataService.createCVData(
            profile(),
            workExperiences,
            education(),
            projects(),
            skills(),
            mapOf("test" to "value")
        )

        val expectedEntry = CVEntry(
            CURRENT_JOB_TITLE,
            "Here",
            "05.2020",
            TODAY,
            TECH_COMPANY,
            "",
            emptyList()
        )

        assertEquals(1, cvData.workExperiences.size)
        assertEquals(expectedEntry, cvData.workExperiences[0])
    }

    private fun workExperiences() = listOf(
        currentJob(),
        previousJob()
    )

    private fun currentJob() = WorkExperienceEntity(
        1,
        CURRENT_JOB_TITLE,
        TECH_COMPANY,
        LocalDate.of(2020, 5, 1),
        null,
        "Here",
        "Tech Stuff",
        mockk()
    )

    private fun previousJob() = WorkExperienceEntity(
        2,
        "Previous Job",
        "Other Inc.",
        LocalDate.of(2010, 1, 1),
        LocalDate.of(2015, 12, 31),
        "There",
        "Other Stuff",
        mockk()
    )

    private fun education() = listOf(
        EducationEntity(
            1,
            "School",
            "City",
            LocalDate.of(2010, 1, 1),
            LocalDate.of(2015, 12, 31),
            "Degree",
            "Major",
            mockk()
        )
    )

    private fun projects() = listOf(
        currentProject(),
        previousProject()
    )

    private fun currentProject() = ProjectEntity(
        1,
        "Current Project",
        "role",
        "description",
        LocalDate.of(2020, 6, 1),
        null,
        emptyList(),
        mockk()
    )

    private fun previousProject() = ProjectEntity(
        2,
        "Previous Project",
        "role",
        "description",
        LocalDate.of(2015, 4, 6),
        LocalDate.of(2017, 7, 15),
        listOf(
            ProjectLink("url", "name", ProjectLinkType.DOCUMENT)
        ),
        mockk()
    )

    private fun skills() = listOf(
        SkillEntity(1, "Spring Boot", FRAMEWORK_SKILLS, 60, mockk()),
        SkillEntity(2, "Spring Data", FRAMEWORK_SKILLS, 50, mockk()),
        SkillEntity(3, "Docker", TECHNOLOGY_SKILLS, 50, mockk()),
        SkillEntity(4, "Java", LANGUAGE_SKILLS, 70, mockk()),
        SkillEntity(5, "Kotlin", LANGUAGE_SKILLS, 80, mockk()),
        SkillEntity(6, "AWS", CLOUD_PLATFORM_SKILLS, 50, mockk()),
        SkillEntity(7, "GCP", CLOUD_PLATFORM_SKILLS, 60, mockk()),
        SkillEntity(8, "JavaScript", LANGUAGE_SKILLS, 80, mockk()),
        SkillEntity(9, "TypeScript", LANGUAGE_SKILLS, 54, mockk()),
        SkillEntity(10, "HTML", LANGUAGE_SKILLS, 50, mockk()),
    )

    private fun profile() = ProfileEntity(
        "Job Title",
        "bio",
        true,
        false,
        false,
        false,
        false,
        "picture",
        1,
        account(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        null
    )

    private fun account() = ApplicantAccountEntity(
        "username",
        "password",
        false,
        true,
        1,
        AccountDetailsEntity(
            "first",
            "last",
            "em@il.com",
            "+41 79 123 45 67",
            LocalDate.of(1985, 6, 25),
            "street",
            "4",
            "12345",
            "city",
            "CH",
            "en",
            1
        )
    )

    private fun fullCVData() = CVData(
        "en",
        "first",
        "last",
        "Job Title",
        "bio",
        "em@il.com",
        "+41 79 123 45 67",
        "street 4, 12345 city",
        "25.06.1985",
        listOf(
            CVEntry(
                CURRENT_JOB_TITLE,
                "Here",
                "05.2020",
                TODAY,
                TECH_COMPANY,
                "Tech Stuff",
                emptyList()
            ),
            CVEntry(
                "Previous Job",
                "There",
                "01.2010",
                "12.2015",
                "Other Inc.",
                "Other Stuff",
                emptyList()
            )
        ),
        listOf(
            CVSkills(
                FRAMEWORK_SKILLS,
                listOf("Spring Boot", "Spring Data"),
            ),
            CVSkills(
                TECHNOLOGY_SKILLS,
                listOf("Docker")
            ),
            CVSkills(
                LANGUAGE_SKILLS,
                listOf("Kotlin", "JavaScript", "Java", "TypeScript", "HTML")
            ),
            CVSkills(
                CLOUD_PLATFORM_SKILLS,
                listOf("GCP", "AWS")
            )
        ),
        listOf(
            CVEntry(
                "Degree",
                "City",
                "01.2010",
                "12.2015",
                "School",
                "Major",
                emptyList()
            )
        ),
        listOf(
            CVEntry(
                "Current Project",
                "",
                "06.2020",
                TODAY,
                "role",
                "description",
                emptyList()
            ),
            CVEntry(
                "Previous Project",
                "",
                "04.2015",
                "07.2017",
                "role",
                "description",
                listOf(
                    CVLink("url", "name", "DOCUMENT")
                )
            )
        ),
        mapOf("test" to "value")
    )
}