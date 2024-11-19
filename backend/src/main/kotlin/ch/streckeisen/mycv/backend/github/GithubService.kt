package ch.streckeisen.mycv.backend.github

import ch.streckeisen.mycv.backend.github.dto.GithubUserEmail
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

private val logger = KotlinLogging.logger {}

@Service
class GithubService(
    restClientBuilder: RestClient.Builder,
    @Value("\${my-cv.github.api-base-url}") private val baseUrl: String,
) {
    private val restClient = restClientBuilder.baseUrl(baseUrl).build()

    fun getUserEmail(accountId: String, accessToken: String): Result<String> {
        logger.debug { "Getting email for account $accountId" }
        val result = restClient.get()
            .uri("/user/emails", accountId)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .exchange<Result<List<GithubUserEmail>>> { _, response ->
                if (response.statusCode.is2xxSuccessful) {
                    val emails = response.bodyTo(object : ParameterizedTypeReference<List<GithubUserEmail>>() {})
                    if (emails == null) {
                        Result.failure(GithubException("Failed to get user email for account $accountId. REST call was successful, but body is invalid"))
                    } else {
                        Result.success(emails)
                    }
                } else {
                    Result.failure(GithubException("Failed to get user email for account $accountId"))
                }
            }
        return result.fold(
            onSuccess = { emailData ->
                val email = emailData.firstOrNull { email -> email.primary && email.verified }
                if (email == null) {
                    Result.failure(GithubException("Failed to get user email for account $accountId. No primary and verified email found"))
                } else {
                    logger.debug { "Got email for account $accountId: ${email.email}" }
                    Result.success(email.email)
                }
            },
            onFailure = {
                logger.error(it) { "Failed to get email for account $accountId" }
                Result.failure(it)
            }
        )
    }
}