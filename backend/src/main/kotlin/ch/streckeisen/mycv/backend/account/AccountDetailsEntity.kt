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
    var firstName: String,
    @Column(name = "lastname")
    var lastName: String,
    var email: String,
    var phone: String,
    var birthday: LocalDate,
    var street: String,
    var houseNumber: String?,
    var postcode: String,
    var city: String,
    var country: String,
    var language: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)