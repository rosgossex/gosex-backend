package gosex.backend

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

  @Value("\${gosex.esia.url}") private lateinit var esiaUrl: String

  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    return http
      .csrf { it.disable() }
      .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
      .authorizeHttpRequests { requests ->
        requests.requestMatchers("/actuator/health").permitAll().anyRequest().authenticated()
      }
      .oauth2ResourceServer { oauth2 ->
        oauth2.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()) }
      }
      .build()
  }

  @Bean
  fun jwtDecoder(): JwtDecoder {
    val jwksUri = "$esiaUrl/realms/gosex/protocol/openid-connect/certs"
    return NimbusJwtDecoder.withJwkSetUri(jwksUri).build()
  }

  @Bean
  fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
    val converter = JwtAuthenticationConverter()
    converter.setJwtGrantedAuthoritiesConverter { jwt: Jwt ->
      val emailVerified = jwt.getClaim<Boolean>("email_verified") ?: false
      if (emailVerified) {
        listOf(SimpleGrantedAuthority("ROLE_USER"))
      } else {
        emptyList()
      }
    }
    return converter
  }
}
