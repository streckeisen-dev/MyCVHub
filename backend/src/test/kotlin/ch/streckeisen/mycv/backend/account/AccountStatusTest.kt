package ch.streckeisen.mycv.backend.account

import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

class AccountStatusTest {
    @Test
    fun testVerifiedAccountStatus() {
        val account = mockk<ApplicantAccountEntity> {
            every { accountDetails } returns mockk()
            every { isVerified } returns true
        }

        val accountStatus = AccountStatus.ofAccount(account)

        assertEquals(AccountStatus.VERIFIED, accountStatus)
    }

    @Test
    fun testUnverifiedAccountStatus() {
        val account = mockk<ApplicantAccountEntity> {
            every { accountDetails } returns mockk()
            every { isVerified } returns false
        }

        val accountStatus = AccountStatus.ofAccount(account)

        assertEquals(AccountStatus.UNVERIFIED, accountStatus)
    }

    @Test
    fun testIncompleteAccountStatus() {
        val account = mockk<ApplicantAccountEntity> {
            every { accountDetails } returns null
        }

        val accountStatus = AccountStatus.ofAccount(account)

        assertEquals(AccountStatus.INCOMPLETE, accountStatus)
    }

    /**
     * This case should never happen, but in case it should, the account privileges should be as low as possible
     * and the account status therefore incomplete.
     */
    @Test
    fun testVerifiedAccountWithoutUserDetails() {
        val account = mockk<ApplicantAccountEntity> {
            every { accountDetails } returns null
            every { isVerified } returns true
        }

        val accountStatus = AccountStatus.ofAccount(account)

        assertEquals(AccountStatus.INCOMPLETE, accountStatus)
    }
}