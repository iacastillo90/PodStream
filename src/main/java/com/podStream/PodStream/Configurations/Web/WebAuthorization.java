package com.podStream.PodStream.Configurations.Web;

import com.podStream.PodStream.Configurations.Security.Jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Clase de configuración de Spring Security para definir políticas de autorización y configurar la cadena de filtros de seguridad.
 * Maneja la autenticación basada en JWT y asegura que las sesiones sean sin estado.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebAuthorization {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WebAuthorization.class);

    /**
     * Define la cadena de filtros de seguridad para configurar las políticas de seguridad de la aplicación.
     *
     * @param http Objeto HttpSecurity para configurar la seguridad.
     * @return Un objeto SecurityFilterChain con las reglas de seguridad.
     * @throws RuntimeException Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            logger.info("Configuring security filter chain");
            return http
                    .csrf(csrf ->
                            csrf
                                    .disable())
                    .authorizeHttpRequests(authRequest ->
                            authRequest
                                    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll() // Añadido para permitir login
                                    .requestMatchers(HttpMethod.POST, "/auth/register").permitAll() // Añadido para permitir registro
                                    //                      .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll() // Añadido para permitir el acceso a la salud
                                    //                      .requestMatchers(HttpMethod.GET, "/actuator/prometheus").permitAll() // Añadido para permitir el acceso a prometheus
                                    //                      .requestMatchers(HttpMethod.GET, "/actuator/metrics").permitAll() // Añadido para permitir el acceso a metrics
                                    .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll() // Añadido para permitir el acceso a swagger
                                    .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll() // Añadido para permitir el acceso a swagger
                                    .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                                    .requestMatchers("/actuator/**").hasRole("ADMIN")
                                    .anyRequest().authenticated()
                    )
                    .sessionManagement(sessionManager->
                            sessionManager
                                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authenticationProvider(authProvider)
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
        } catch (Exception e) {
            logger.error("Error configuring security filter chain: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}