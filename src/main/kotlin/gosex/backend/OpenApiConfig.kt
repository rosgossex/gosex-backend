package gosex.backend

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

  @Bean
  fun customOpenAPI(): OpenAPI {
    return OpenAPI()
      .info(
        Info()
          .title("GoSex Backend API")
          .description("Dating application backend service with ESIA authentication")
          .version("1.0.0")
      )
      .components(
        Components()
          .addSecuritySchemes(
            "bearerAuth",
            SecurityScheme()
              .type(SecurityScheme.Type.HTTP)
              .scheme("bearer")
              .bearerFormat("JWT")
              .description("JWT token from ESIA authentication"),
          )
      )
  }
}
