package ch.streckeisen.mycv.backend.account

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDate

@Entity
class AccountDetailsEntity(
    @Column(name = "firstname")
    val firstName: String,
    @Column(name = "lastname")
    val lastName: String,
    val email: String,
    val phone: String,
    val birthday: LocalDate,
    val street: String,
    val houseNumber: String?,
    val postcode: String,
    val city: String,
    val country: String,
    val language: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
}