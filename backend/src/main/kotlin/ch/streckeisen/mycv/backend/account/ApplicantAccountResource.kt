package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountDto
import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.LoginResponseDto
import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account")
class ApplicantAccountResource(private val applicantAccountService: ApplicantAccountService) {
    @GetMapping
    fun getAccount(): ResponseEntity<AccountDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()
        return applicantAccountService.findById(principal.id)
            .fold(
                onSuccess = { account ->
                    ResponseEntity.ok(account.toAccountDto())
                },
                onFailure = {
                    ResponseEntity.internalServerError().build()
                }
            )
    }

    @PostMapping
    fun updateAccount(@RequestBody accountUpdate: AccountUpdateDto): ResponseEntity<AccountDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()
        return applicantAccountService.update(principal.id, accountUpdate)
            .fold(
                onSuccess = { updatedAccount ->
                    ResponseEntity.ok(updatedAccount.toAccountDto())
                },
                onFailure = {
                    throw it
                }
            )
    }

    @PostMapping("/change-password")
    fun changePassword(@RequestBody changePasswordDto: ChangePasswordDto): ResponseEntity<LoginResponseDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return applicantAccountService.changePassword(principal.id, changePasswordDto)
            .fold(
                onSuccess = {
                    ResponseEntity.ok().build()
                },
                onFailure = {
                    throw it
                }
            )
    }
}