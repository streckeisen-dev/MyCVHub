package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountDto
import ch.streckeisen.mycv.backend.account.dto.LinkedAccount

fun ApplicantAccountEntity.toAccountDto(): AccountDto {
    val details = accountDetails!!
    return AccountDto(
        username,
        details.firstName,
        details.lastName,
        details.email,
        details.phone,
        details.birthday,
        details.street,
        details.houseNumber,
        details.postcode,
        details.city,
        details.country,
        details.language,
        profile != null,
        password != null,
        oauthIntegrations.map { LinkedAccount(it.id.type.name) }
    )
}
