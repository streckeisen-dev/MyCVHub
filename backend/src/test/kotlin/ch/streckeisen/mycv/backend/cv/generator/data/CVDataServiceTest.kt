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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.time.LocalDate
import java.util.Locale
import kotlin.test.assertEquals

class CVDataServiceTest {
    private lateinit var messagesService: MessagesService
    private lateinit var cvDataService: CVDataService

    @BeforeEach
    fun setup() {
        messagesService = mockk(relaxed = true) {
            every { getMessage(eq("$MYCV_KEY_PREFIX.date.today")) } returns "Today"
        }
        cvDataService = CVDataService(messagesService)
    }

    @Test
    fun testPrepareWorkExperienceWithoutFilter() {
        val experiences = workExperiences()

        val result = cvDataService.prepareWorkExperiences(experiences, null)

        assertEquals(experiences.size, result.size)
        assertEquals(experiences, result)
    }

    @Test
    fun testPrepareWorkExperienceWithIdFilter() {
        val experiences = workExperiences()
        val includedItems = listOf(IncludedCVItem(1, true))

        val result = cvDataService.prepareWorkExperiences(experiences, includedItems)

        assertEquals(includedItems.size, result.size)
        assertEquals(listOf(experiences[0]), result)
    }

    @Test
    fun testPrepareWorkExperienceWithIdFilterAndNoResults() {
        val experiences = workExperiences()
        val includedItems = listOf<IncludedCVItem>()

        val result = cvDataService.prepareWorkExperiences(experiences, includedItems)

        assertEquals(0, result.size)
    }

    @Test
    fun testPrepareWorkExperienceWithDescriptionFilter() {
        val experiences = workExperiences()
        val includedItems = listOf(IncludedCVItem(1, false), IncludedCVItem(2, true))

        val result = cvDataService.prepareWorkExperiences(experiences, includedItems)

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
    fun testPrepareEducationWithoutFilter() {
        val education = education()

        val result = cvDataService.prepareEducation(education, null)

        assertEquals(education.size, result.size)
        assertEquals(education, result)
    }

    @Test
    fun testPrepareEducationWithIdFilter() {
        val education = education()
        val includedItems = listOf(IncludedCVItem(1, true))

        val result = cvDataService.prepareEducation(education, includedItems)

        assertEquals(includedItems.size, result.size)
        assertEquals(listOf(education[0]), result)
    }

    @Test
    fun testPrepareEducationWithIdFilterAndNoResults() {
        val education = education()

        val result = cvDataService.prepareEducation(education, listOf())

        assertEquals(0, result.size)
    }

    @Test
    fun testPrepareEducationWithDescriptionFilter() {
        val education = education()
        val includedItems = listOf(IncludedCVItem(1, false))

        val result = cvDataService.prepareEducation(education, includedItems)

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
    fun testPrepareProjectsWithoutFilter() {
        val projects = projects()

        val result = cvDataService.prepareProjects(projects, null)

        assertEquals(projects.size, result.size)
        assertEquals(projects, result)
    }

    @Test
    fun testPrepareProjectsWithIdFilter() {
        val projects = projects()
        val includedItems = listOf(IncludedCVItem(1, true))

        val result = cvDataService.prepareProjects(projects, includedItems)

        assertEquals(includedItems.size, result.size)
        assertEquals(listOf(projects[0]), result)
    }

    @Test
    fun testPrepareProjectsWithIdFilterAndNoResults() {
        val projects = projects()

        val result = cvDataService.prepareProjects(projects, listOf())

        assertEquals(0, result.size)
    }

    @Test
    fun testPrepareProjectsWithDescriptionFilter() {
        val projects = projects()
        val includedItems = listOf(IncludedCVItem(1, false), IncludedCVItem(2, true))

        val result = cvDataService.prepareProjects(projects, includedItems)

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
    fun testPrepareSkillsWithoutFilter() {
        val skills = skills()

        val result = cvDataService.prepareSkills(skills, null)

        assertEquals(skills.size, result.size)
        assertEquals(skills, result)
    }

    @Test
    fun testPrepareSkillsWithIdFilter() {
        val skills = skills()
        val includedItems = listOf(IncludedCVItem(1, true), IncludedCVItem(3, true))

        val result = cvDataService.prepareSkills(skills, includedItems)

        assertEquals(includedItems.size, result.size)
        assertEquals(listOf(skills[0], skills[2]), result)
    }

    @Test
    fun testPrepareSkillsWithIdFilterAndNoResults() {
        val skills = skills()

        val result = cvDataService.prepareSkills(skills, listOf())

        assertEquals(0, result.size)
    }

    @Test
    fun testPrepareSkillsWithDescriptionFilter() {
        val skills = skills()
        val includedItems = listOf(IncludedCVItem(1, false), IncludedCVItem(2, true))

        val result = cvDataService.prepareSkills(skills, includedItems)

        assertEquals(includedItems.size, result.size)
        assertEquals(listOf(skills[0], skills[1]), result)
    }

    @Test
    fun testCreateCVData() {
        val profile = profile()

        val cvData = cvDataService.createCVData(
            Locale.ENGLISH,
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
        val workExperiences = cvDataService.prepareWorkExperiences(workExperiences(), listOf(IncludedCVItem(1, false)))

        val cvData = cvDataService.createCVData(
            Locale.ENGLISH,
            profile(),
            workExperiences,
            education(),
            projects(),
            skills(),
            mapOf("test" to "value")
        )

        val expectedEntry = CVEntry(
            "Current Job",
            "Here",
            "05.2020",
            "Today",
            "Tech Inc.",
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
        "Current Job",
        "Tech Inc.",
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
        LocalDate.of(2020, 5, 1),
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
        SkillEntity(1, "Spring Boot", "Framework", 60, mockk()),
        SkillEntity(2, "Spring Data", "Framework", 50, mockk()),
        SkillEntity(3, "Docker", "Technology", 50, mockk()),
        SkillEntity(4, "Java", "Language", 70, mockk()),
        SkillEntity(5, "Kotlin", "Language", 80, mockk()),
        SkillEntity(6, "AWS", "Cloud Platform", 50, mockk()),
        SkillEntity(7, "GCP", "Cloud Platform", 60, mockk()),
        SkillEntity(8, "JavaScript", "Language", 80, mockk()),
        SkillEntity(9, "TypeScript", "Language", 54, mockk()),
        SkillEntity(10, "HTML", "Language", 50, mockk()),
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
                "Current Job",
                "Here",
                "05.2020",
                "Today",
                "Tech Inc.",
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
                "Framework",
                listOf("Spring Boot", "Spring Data"),
            ),
            CVSkills(
                "Technology",
                listOf("Docker")
            ),
            CVSkills(
                "Language",
                listOf("Kotlin", "JavaScript", "Java", "TypeScript", "HTML")
            ),
            CVSkills(
                "Cloud Platform",
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
                "05.2020",
                "Today",
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