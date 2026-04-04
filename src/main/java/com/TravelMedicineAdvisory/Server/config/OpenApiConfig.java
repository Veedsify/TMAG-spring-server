package com.TravelMedicineAdvisory.Server.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tmagOpenApi(
            @Value("${app.version}") String version,
            @Value("${app.host:http://localhost:8080}") String host,
            @Value("${app.admin.email:hello@tmag.health}") String adminEmail) {
        return new OpenAPI()
                .info(new Info()
                        .title("Travel Medicine Advisory Global API")
                        .version(version)
                        .description("""
                                REST API for TMAG: personalized travel health guidance, AI travel plans, credits, companies, onboarding, and admin surfaces.

                                **Authentication:** Most routes require a JWT from `POST /api/v1/auth/login` or registration / invitation flows; send `Authorization: Bearer <token>`. Platform admin and company-admin portals use `/api/v1/admin/auth/login` and `/api/v1/company-admin/auth/login`.

                                **API key:** When enabled, requests may require `X-Api-Key` (`APP_API_KEY`). Use **Authorize** in Swagger UI to attach Bearer token and API key for Try it out.

                                **Route areas:** `/api/v1/**` main app and HR user flows; `/api/v1/admin/**` platform administration; `/api/v1/company-admin/**` company (HR) portal. Some GET routes (countries, blog posts, FAQ, system settings, health alerts) are public — see `SecurityConfig`.

                                **Docs:** OpenAPI JSON at `/v3/api-docs`; UI at `/swagger-ui.html`.
                                """)
                        .contact(new Contact()
                                .name("TMAG Support")
                                .email(adminEmail))
                        .license(new License().name("Proprietary").url("https://tmag.health")))
                .servers(List.of(new Server().url(host).description("Configured API base (`app.host`)")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT returned by auth endpoints"))
                        .addSecuritySchemes("api-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-Api-Key")
                                        .description("Application API key when middleware requires it")));
    }
}
