package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountDto
import ch.streckeisen.mycv.backend.cv.applicant.Applicant

fun Applicant.toAccountDto(): AccountDto = AccountDto(
    firstName,
    lastName,
    email,
    phone,
    birthday,
    street,
    houseNumber,
    postcode,
    city,
    country
)