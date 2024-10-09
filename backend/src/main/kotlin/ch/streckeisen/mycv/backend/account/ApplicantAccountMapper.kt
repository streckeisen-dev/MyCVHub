package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountDto

fun ApplicantAccountEntity.toAccountDto(): AccountDto = AccountDto(
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
    profile = profile?.alias
)