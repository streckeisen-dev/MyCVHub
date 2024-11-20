package ch.streckeisen.mycv.backend.upgrade

abstract class UpgradeTask(
    val id: Int,
    val name: String
) {
    abstract fun execute(): Result<Unit>
}