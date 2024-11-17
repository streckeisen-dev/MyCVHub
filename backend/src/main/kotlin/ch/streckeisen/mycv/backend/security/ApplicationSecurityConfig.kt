package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.auth.oauth.MyCvOAuth2AuthorizationRequestResolver
import ch.streckeisen.mycv.backend.account.auth.oauth.OAuth2SuccessHandler
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
@Configuration
class ApplicationSecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val authenticationProvider: AuthenticationProvider,
    private val messagesService: MessagesService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val clientRegistrationRepository: ClientRegistrationRepository,
    @Value("\${my-cv.frontend.base-url}")
    private val frontendBaseUrl: String
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { requests ->
                requests.anyRequest().permitAll()
            }
            .formLogin { login ->
                login.disable()
            }
            .logout { logout ->
                logout.disable()
            }
            .httpBasic { basic ->
                basic.disable()
            }
            .oauth2Login { oauth2 ->
                oauth2.loginPage("/api/auth/oauth2")
                oauth2.authorizationEndpoint { authorizationEndpoint ->
                    val baseResolver = DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/api/auth/oauth2/authorization")
                    authorizationEndpoint.authorizationRequestResolver(MyCvOAuth2AuthorizationRequestResolver(baseResolver))
                }
                oauth2.redirectionEndpoint { redirectionEndpoint ->
                    redirectionEndpoint.baseUri("/api/auth/oauth2/callback/*")
                }
                oauth2.successHandler(oAuth2SuccessHandler)
                oauth2.failureUrl("${frontendBaseUrl}/login/oauth-failure")
            }
            .sessionManagement { sessionManager ->
                sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { authenticationException ->
                authenticationException.authenticationEntryPoint(AuthenticationExceptionHandler(messagesService))
                authenticationException.accessDeniedHandler(MyCVAccessDeniedHandler(messagesService))
            }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.addAllowedOrigin("http://localhost:3000")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}