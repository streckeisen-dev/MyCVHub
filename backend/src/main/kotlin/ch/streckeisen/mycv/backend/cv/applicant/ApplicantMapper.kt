package ch.streckeisen.mycv.backend.cv.applicant

import ch.streckeisen.mycv.backend.account.AccountDto
import ch.streckeisen.mycv.backend.cv.applicant.publicapi.PublicApplicantDto

fun Applicant.toDto() = PublicApplicantDto(
    id,
    firstName,
    lastName,
    email,
    phone,
    birthday,
    AddressDto(street, houseNumber, postcode, city, country),
    null,
    null,
    null
)

fun Applicant.toAccountDto() = AccountDto(
    firstName,
    lastName,
    email,
    phone,
    birthday,
    street,
    houseNumber,
    postcode,
    city,
    country,
    hasPublicProfile
)