package ch.streckeisen.mycv.backend.cv.project

import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.executeParameterizedTest
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

private const val VALID_URL = "https://example.com"

class ProjectValidationServiceTest {
    private lateinit var messagesService: MessagesService
    private lateinit var projectValidationService: ProjectValidationService

    @BeforeEach
    fun setup() {
        messagesService = mockk(relaxed = true)
        projectValidationService = ProjectValidationService(messagesService)
    }

    @ParameterizedTest
    @MethodSource("projectValidationTestDataProvider")
    fun testValidateProject(projectUpdate: ProjectUpdateDto, isValid: Boolean, numberOfErrors: Int) {
        executeParameterizedTest(projectUpdate, isValid, numberOfErrors, projectValidationService::validateProject)
    }

    companion object {
        @JvmStatic
        fun projectValidationTestDataProvider() = listOf(
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = null,
                    projectStart = null,
                    projectEnd = null,
                    links = listOf()
                ),
                false,
                4
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = "n".repeat(PROJECT_NAME_MAX_LENGTH + 1),
                    role = null,
                    description = null,
                    projectStart = null,
                    projectEnd = null,
                    links = listOf()
                ),
                false,
                4
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = "name",
                    role = null,
                    description = null,
                    projectStart = null,
                    projectEnd = null,
                    links = listOf()
                ),
                false,
                3
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = "r".repeat(ROLE_MAX_LENGTH + 1),
                    description = null,
                    projectStart = null,
                    projectEnd = null,
                    links = listOf()
                ),
                false,
                4
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = "role",
                    description = null,
                    projectStart = null,
                    projectEnd = null,
                    links = listOf()
                ),
                false,
                3
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = "description",
                    projectStart = null,
                    projectEnd = null,
                    links = listOf()
                ),
                false,
                3
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = null,
                    projectStart = LocalDate.now().plusDays(1),
                    projectEnd = null,
                    links = listOf()
                ),
                false,
                4
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = null,
                    projectStart = LocalDate.of(2021, 1, 1),
                    projectEnd = null,
                    links = listOf()
                ),
                false,
                3
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = null,
                    projectStart = null,
                    projectEnd = LocalDate.now().plusDays(1),
                    links = listOf()
                ),
                false,
                5
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = null,
                    projectStart = LocalDate.of(2021, 1, 1),
                    projectEnd = LocalDate.of(2020, 12, 31),
                    links = listOf()
                ),
                false,
                4
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = null,
                    projectStart = LocalDate.of(2021, 1, 1),
                    projectEnd = LocalDate.of(2021, 12, 31),
                    links = listOf()
                ),
                false,
                3
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = null,
                    projectStart = null,
                    projectEnd = null,
                    links = listOf(
                        ProjectLinkUpdateDto(
                            url = null,
                            type = null
                        ),
                        ProjectLinkUpdateDto(
                            url = null,
                            type = null
                        )
                    )
                ),
                false,
                8
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = null,
                    projectStart = null,
                    projectEnd = null,
                    links = listOf(
                        ProjectLinkUpdateDto(
                            url = "url",
                            type = null
                        )
                    )
                ),
                false,
                6
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = null,
                    projectStart = null,
                    projectEnd = null,
                    links = listOf(
                        ProjectLinkUpdateDto(
                            url = VALID_URL,
                            type = null
                        )
                    )
                ),
                false,
                5
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = null,
                    projectStart = null,
                    projectEnd = null,
                    links = listOf(
                        ProjectLinkUpdateDto(
                            url = null,
                            type = ProjectLinkType.WEBSITE
                        )
                    )
                ),
                false,
                5
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = null,
                    role = null,
                    description = null,
                    projectStart = null,
                    projectEnd = null,
                    links = listOf(
                        ProjectLinkUpdateDto(
                            url = VALID_URL,
                            type = ProjectLinkType.WEBSITE
                        )
                    )
                ),
                false,
                4
            ),
            Arguments.of(
                ProjectUpdateDto(
                    id = null,
                    name = "Project",
                    role = "Role",
                    description = "Description",
                    projectStart = LocalDate.of(2021, 1, 1),
                    projectEnd = LocalDate.of(2021, 12, 31),
                    links = listOf(
                        ProjectLinkUpdateDto(
                            url = VALID_URL,
                            type = ProjectLinkType.WEBSITE
                        )
                    )
                ),
                true,
                0
            )
        )
    }
}