package com.TravelMedicineAdvisory.Server.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.TravelMedicineAdvisory.Server.security.CustomUserDetailsService;
import com.TravelMedicineAdvisory.Server.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.cors.enabled:true}")
    private boolean corsEnabled;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH,HEAD}")
    private String allowedMethods;

    @Value("${app.cors.expose-headers:Content-Length,Content-Type}")
    private String exposeHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:43200}")
    private long maxAge;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"message\":\"Unauthorized\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"message\":\"Access denied\"}");
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/public/**",
                                "/api/v1/test/**",
                                "/api/v1/admin/auth/login",
                                "/api/v1/admin/auth/logout",
                                "/api/v1/company-admin/auth/login",
                                "/api/v1/company-admin/auth/logout",
                                "/api/v1/payments/webhook/**",
                                "/api/v1/credit-purchases/callback",
                                "/api/v1/company-admin/credits/callback",
                                "/api/v1/ebooks/callback",
                                "/ws/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/health",
                                "/",
                                "/storage/**")
                        .permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/v1/countries",
                                "/api/v1/countries/**",
                                "/api/v1/faq-items",
                                "/api/v1/faq-items/**",
                                "/api/v1/country-health-alerts",
                                "/api/v1/country-health-alerts/**",
                                "/api/v1/blog-posts",
                                "/api/v1/blog-posts/**",
                                "/api/v1/system-settings",
                                "/api/v1/system-settings/**",
                                "/api/v1/ebooks",
                                "/api/v1/ebooks/*",
                                "/api/v1/ebooks/orders/*",
                                "/api/v1/exchange-rates",
                                "/api/v1/exchange-rates/**",
                                "/api/v1/user-credit-plans",
                                "/api/v1/user-credit-plans/**")
                        .permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST,
                                "/api/v1/contact",
                                "/api/v1/newsletter/subscribe",
                                "/api/v1/ebooks/checkout",
                                "/api/v1/ebooks/orders/verify",
                                "/api/v1/cart/checkout")
                        .permitAll()
                        .requestMatchers("/api/v1/cart", "/api/v1/cart/**")
                        .authenticated()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (corsEnabled) {
            // setAllowedOriginPatterns is required in Spring Security 6 when
            // allowCredentials=true;
            // setAllowedOrigins causes a validation failure with credentials in newer
            // versions.
            configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
            configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
            configuration.addAllowedHeader("*");
            configuration.setExposedHeaders(Arrays.asList(exposeHeaders.split(",")));
            configuration.setAllowCredentials(allowCredentials);
            configuration.setMaxAge(maxAge);
        }
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
