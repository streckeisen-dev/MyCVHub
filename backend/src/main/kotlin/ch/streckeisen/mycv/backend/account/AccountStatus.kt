package ch.streckeisen.mycv.backend.account

enum class AccountStatus(val permissionValue: Int) {
    INCOMPLETE(1),
    UNVERIFIED(2),
    VERIFIED(3);

    companion object {
        fun ofAccount(account: ApplicantAccountEntity): AccountStatus {
            return when {
                account.accountDetails == null -> INCOMPLETE
                account.isVerified -> VERIFIED
                else -> UNVERIFIED
            }
        }
    }
}