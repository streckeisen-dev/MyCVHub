package ch.streckeisen.mycv.backend.publicapi.profile.dto

data class PublicAddressDto(
    val street: String,
    val houseNumber: String?,
    val postcode: String,
    val city: String,
    val country: String
)
