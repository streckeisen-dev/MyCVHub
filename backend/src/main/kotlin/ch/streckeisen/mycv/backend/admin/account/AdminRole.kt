package ch.streckeisen.mycv.backend.admin.account

enum class AdminRole(val permissionValue: Int) {
    ADMIN(1),
    SUPER_ADMIN(2)
}
