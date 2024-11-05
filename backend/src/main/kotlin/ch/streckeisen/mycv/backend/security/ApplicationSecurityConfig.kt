package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.oauth.OAuth2SuccessHandler
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
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
    @Value("\${frontend.base-url}")
    private val frontendBaseUrl: String
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/", "index.html", "/assets/**", "/ui/**").permitAll()
                    .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2.loginPage("/api/auth/oauth2")
                oauth2.authorizationEndpoint { authorizationEndpoint ->
                    authorizationEndpoint.baseUri("/api/auth/oauth2/authorization")
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