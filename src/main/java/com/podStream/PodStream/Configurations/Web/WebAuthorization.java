package com.podStream.PodStream.Configurations.Web;

import com.podStream.PodStream.Configurations.Security.Jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebAuthorization {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthenticationProvider authProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        return http
            .csrf(csrf -> 
                csrf
                .disable())
            .authorizeHttpRequests(authRequest ->
              authRequest
                      .requestMatchers(HttpMethod.POST, "/auth/login").permitAll() // Añadido para permitir login
                      .requestMatchers(HttpMethod.POST, "/auth/register").permitAll() // Añadido para permitir registro
                      .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll() // Añadido para permitir el acceso a la salud
                      .requestMatchers(HttpMethod.GET, "/actuator/prometheus").permitAll() // Añadido para permitir el acceso a prometheus
                      .requestMatchers(HttpMethod.GET, "/actuator/metrics").permitAll() // Añadido para permitir el acceso a metrics
                      .requestMatchers(HttpMethod.GET, "/swagger-ui**/**").permitAll() // Añadido para permitir el acceso a swagger
                      .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll() // Añadido para permitir el acceso a swagger
                .anyRequest().authenticated()
                )
            .sessionManagement(sessionManager->
                sessionManager 
                  .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
            
            
    }

}
