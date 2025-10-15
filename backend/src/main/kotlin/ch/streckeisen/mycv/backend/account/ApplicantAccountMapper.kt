package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountDto

fun ApplicantAccountEntity.toAccountDto(): AccountDto = AccountDto(
    username,
    accountDetails!!.firstName,
    accountDetails.lastName,
    accountDetails.email,
    accountDetails.phone,
    accountDetails.birthday,
    accountDetails.street,
    accountDetails.houseNumber,
    accountDetails.postcode,
    accountDetails.city,
    accountDetails.country,
    accountDetails.language,
    profile != null,
    password != null
)