package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.security.annotations.PublicApi
import ch.streckeisen.mycv.backend.security.annotations.RequiresAccountStatus
import io.mockk.every
import io.mockk.mockk
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import java.lang.reflect.Method
import java.util.Locale

class EndpointSecurityAspectTest {
    private lateinit var endpointSecurityAspect: EndpointSecurityAspect

    @BeforeEach
    fun setup() {
        endpointSecurityAspect = EndpointSecurityAspect()
    }

    @ParameterizedTest
    @MethodSource("authorizeTestDataProvider")
    fun testAuthorize(accessedMethod: Method, authentication: Authentication?, shouldAllowAccess: Boolean) {
        SecurityContextHolder.getContext().authentication = authentication
        val joinPoint = mockJoinPoint(accessedMethod)

        try {
            endpointSecurityAspect.authorize(joinPoint)
            if (!shouldAllowAccess) {
                fail("Access should have been denied by throwing an exception")
            }
        } catch (ex: Exception) {
            if (shouldAllowAccess) {
                fail("Should not throw exception", ex)
            } else {
                assertTrue(ex is AccessDeniedException || ex is AuthenticationException)
            }
        }
    }

    private fun mockJoinPoint(accessedMethod: Method): JoinPoint {
        val joinPoint = mockk<JoinPoint> {
            every { signature } returns mockk<MethodSignature> {
                every { method } returns accessedMethod
                every { declaringType } returns accessedMethod.declaringClass
            }
        }
        return joinPoint
    }

    @PublicApi
    private class PublicApiTest {
        fun test() {
            // no body required for testing
        }
    }

    private class RequiresAccountStatusTest {
        @RequiresAccountStatus(AccountStatus.VERIFIED)
        fun testRequiresVerified() {
            // no body required for testing
        }

        @RequiresAccountStatus(AccountStatus.UNVERIFIED)
        fun testRequiresUnverified() {
            // no body required for testing
        }

        @RequiresAccountStatus(AccountStatus.INCOMPLETE)
        fun testRequiresIncomplete() {
            // no body required for testing
        }
    }

    private class RequiresExactAccountStatusTest {
        @RequiresAccountStatus(AccountStatus.VERIFIED, exact = true)
        fun testRequiresVerified() {
            // no body required for testing
        }

        @RequiresAccountStatus(AccountStatus.UNVERIFIED, exact = true)
        fun testRequiresUnverified() {
            // no body required for testing
        }

        @RequiresAccountStatus(AccountStatus.INCOMPLETE, exact = true)
        fun testRequiresIncomplete() {
            // no body required for testing
        }
    }

    @RequiresAccountStatus(AccountStatus.UNVERIFIED)
    private class ClassLevelRequiresAccountStatusTest {
        fun testClassLevelAccountStatus() {
            // no body required for testing
        }
    }

    private class DefaultAuthorizationLevelTest {
        fun testDefault() {
            // no body required for testing
        }
    }

    companion object {
        @JvmStatic
        fun authorizeTestDataProvider() = listOf(
            Arguments.of(
                PublicApiTest::class.java.getMethod("test"),
                null,
                true
            ),
            Arguments.of(
                PublicApiTest::class.java.getMethod("test"),
                mockAnonymousUser(),
                true
            ),
            Arguments.of(
                PublicApiTest::class.java.getMethod("test"),
                mockPrincipal(AccountStatus.INCOMPLETE),
                true
            ),
            Arguments.of(
                PublicApiTest::class.java.getMethod("test"),
                mockPrincipal(AccountStatus.UNVERIFIED),
                true
            ),
            Arguments.of(
                PublicApiTest::class.java.getMethod("test"),
                mockPrincipal(AccountStatus.VERIFIED),
                true
            ),

            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresVerified"),
                null,
                false
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresVerified"),
                mockAnonymousUser(),
                false
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresVerified"),
                mockPrincipal(AccountStatus.INCOMPLETE),
                false
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresVerified"),
                mockPrincipal(AccountStatus.UNVERIFIED),
                false
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresVerified"),
                mockPrincipal(AccountStatus.VERIFIED),
                true
            ),

            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresUnverified"),
                null,
                false
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresUnverified"),
                mockAnonymousUser(),
                false
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresUnverified"),
                mockPrincipal(AccountStatus.INCOMPLETE),
                false
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresUnverified"),
                mockPrincipal(AccountStatus.UNVERIFIED),
                true
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresUnverified"),
                mockPrincipal(AccountStatus.VERIFIED),
                true
            ),

            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresIncomplete"),
                null,
                false
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresIncomplete"),
                mockAnonymousUser(),
                false
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresIncomplete"),
                mockPrincipal(AccountStatus.INCOMPLETE),
                true
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresIncomplete"),
                mockPrincipal(AccountStatus.UNVERIFIED),
                true
            ),
            Arguments.of(
                RequiresAccountStatusTest::class.java.getMethod("testRequiresIncomplete"),
                mockPrincipal(AccountStatus.VERIFIED),
                true
            ),

            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresVerified"),
                null,
                false
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresVerified"),
                mockAnonymousUser(),
                false
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresVerified"),
                mockPrincipal(AccountStatus.INCOMPLETE),
                false
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresVerified"),
                mockPrincipal(AccountStatus.UNVERIFIED),
                false
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresVerified"),
                mockPrincipal(AccountStatus.VERIFIED),
                true
            ),

            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresUnverified"),
                null,
                false
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresUnverified"),
                mockAnonymousUser(),
                false
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresUnverified"),
                mockPrincipal(AccountStatus.INCOMPLETE),
                false
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresUnverified"),
                mockPrincipal(AccountStatus.UNVERIFIED),
                true
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresUnverified"),
                mockPrincipal(AccountStatus.VERIFIED),
                false
            ),

            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresIncomplete"),
                null,
                false
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresIncomplete"),
                mockAnonymousUser(),
                false
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresIncomplete"),
                mockPrincipal(AccountStatus.INCOMPLETE),
                true
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresIncomplete"),
                mockPrincipal(AccountStatus.UNVERIFIED),
                false
            ),
            Arguments.of(
                RequiresExactAccountStatusTest::class.java.getMethod("testRequiresIncomplete"),
                mockPrincipal(AccountStatus.VERIFIED),
                false
            ),
            Arguments.of(
                ClassLevelRequiresAccountStatusTest::class.java.getMethod("testClassLevelAccountStatus"),
                null,
                false
            ),
            Arguments.of(
                ClassLevelRequiresAccountStatusTest::class.java.getMethod("testClassLevelAccountStatus"),
                mockAnonymousUser(),
                false
            ),
            Arguments.of(
                ClassLevelRequiresAccountStatusTest::class.java.getMethod("testClassLevelAccountStatus"),
                mockPrincipal(AccountStatus.INCOMPLETE),
                false
            ),
            Arguments.of(
                ClassLevelRequiresAccountStatusTest::class.java.getMethod("testClassLevelAccountStatus"),
                mockPrincipal(AccountStatus.UNVERIFIED),
                true
            ),
            Arguments.of(
                ClassLevelRequiresAccountStatusTest::class.java.getMethod("testClassLevelAccountStatus"),
                mockPrincipal(AccountStatus.VERIFIED),
                true
            ),
            Arguments.of(
                DefaultAuthorizationLevelTest::class.java.getMethod("testDefault"),
                null,
                false
            ),
            Arguments.of(
                DefaultAuthorizationLevelTest::class.java.getMethod("testDefault"),
                mockAnonymousUser(),
                false
            ),
            Arguments.of(
                DefaultAuthorizationLevelTest::class.java.getMethod("testDefault"),
                mockPrincipal(AccountStatus.INCOMPLETE),
                false
            ),
            Arguments.of(
                DefaultAuthorizationLevelTest::class.java.getMethod("testDefault"),
                mockPrincipal(AccountStatus.UNVERIFIED),
                false
            ),
            Arguments.of(
                DefaultAuthorizationLevelTest::class.java.getMethod("testDefault"),
                mockPrincipal(AccountStatus.VERIFIED),
                true
            )
        )

        private fun mockAnonymousUser(): AnonymousAuthenticationToken {
            return mockk {
                every { isAuthenticated } returns false
            }
        }

        private fun mockPrincipal(status: AccountStatus): UsernamePasswordAuthenticationToken {
            return mockk {
                every { principal } returns MyCvPrincipal("user", 1, status, Locale.ENGLISH)
                every { isAuthenticated } returns true
            }
        }
    }
}