package com.podStream.PodStream.Configurations;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

/**
 * Clase de configuración de Spring para definir beans relacionados con la seguridad de la aplicación.
 * Proporciona un bean de tipo AuthenticationManager para manejar la autenticación de usuarios.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ApplicationConfig.class);

    /**
     * Define un bean de tipo AuthenticationManager para ser usado por Spring Security.
     *
     * @param config La configuración de autenticación proporcionada por Spring.
     * @return Un objeto AuthenticationManager para manejar la autenticación.
     * @throws RuntimeException Si ocurre un error al crear el AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        try {
            logger.info("Creating AuthenticationManager bean");
            return config.getAuthenticationManager();
        } catch (Exception e) {
            logger.error("Error creating AuthenticationManager bean: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }
}