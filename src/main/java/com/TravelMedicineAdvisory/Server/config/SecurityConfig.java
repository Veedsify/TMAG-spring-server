package com.TravelMedicineAdvisory.Server.config;

import com.TravelMedicineAdvisory.Server.security.CustomUserDetailsService;
import com.TravelMedicineAdvisory.Server.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.util.Arrays;
import java.util.List;

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

    @Value("${app.cors.allowed-headers:Origin,Content-Type,Accept,Authorization,X-Api-Key,Base-Orgid}")
    private String allowedHeaders;

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
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/api/v1/public/**",
                    "/ws/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/health",
                    "/",
                    "/storage/**"
                ).permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET,
                    "/api/v1/countries",
                    "/api/v1/countries/**",
                    "/api/v1/faqitems",
                    "/api/v1/faqitems/**",
                    "/api/v1/pricingplans",
                    "/api/v1/pricingplans/**",
                    "/api/v1/countryhealthalerts",
                    "/api/v1/countryhealthalerts/**",
                    "/api/v1/blogposts",
                    "/api/v1/blogposts/**",
                    "/api/v1/systemsettings",
                    "/api/v1/systemsettings/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (corsEnabled) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
            configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
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
