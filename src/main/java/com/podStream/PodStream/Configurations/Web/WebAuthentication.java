package com.podStream.PodStream.Configurations.Web;

import com.podStream.PodStream.Models.User.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase de configuración de Spring para definir beans relacionados con la autenticación web.
 * Configura el codificador de contraseñas, el servicio de detalles de usuario y el proveedor de autenticación.
 */
@Configuration
public class WebAuthentication {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(WebAuthentication.class);

    /**
     * Constructor que inyecta el repositorio de usuarios.
     *
     * @param userRepository El repositorio para acceder a los datos de los usuarios.
     */
    public WebAuthentication(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Define un bean de tipo PasswordEncoder que utiliza el algoritmo BCrypt.
     *
     * @return Un objeto PasswordEncoder para codificar contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define un bean de tipo UserDetailsService para cargar los detalles de un usuario por su nombre de usuario.
     *
     * @return Un objeto UserDetailsService que busca usuarios en el repositorio.
     * @throws UsernameNotFoundException Si el usuario no se encuentra.
     */
    @Bean
    public UserDetailsService userDetailService() {
        return username -> {
            logger.info("Attempting to load user: {}", username);
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("User not found: {}", username);
                        return new UsernameNotFoundException("User not found");
                    });
        };
    }

    /**
     * Define un bean de tipo AuthenticationProvider para manejar la autenticación.
     *
     * @return Un objeto AuthenticationProvider configurado con UserDetailsService y PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
}