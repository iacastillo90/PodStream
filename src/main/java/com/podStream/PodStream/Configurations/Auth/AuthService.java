package com.podStream.PodStream.Configurations.Auth;

import com.podStream.PodStream.Configurations.Security.Jwt.JwtService;
import com.podStream.PodStream.Models.Request.LoginRequest;
import com.podStream.PodStream.Models.Request.RegisterRequest;
import com.podStream.PodStream.Models.Response.AuthResponse;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Models.User.Role;
import com.podStream.PodStream.Models.User.Person;
import com.podStream.PodStream.Repositories.Jpa.PersonRepository;
import com.podStream.PodStream.Services.CartService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la gestión de autenticación y registro de usuarios.
 * Proporciona lógica para iniciar sesión y registrar nuevos usuarios, generando tokens JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CartService cartService;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * Autentica a un usuario y genera un token JWT para el acceso.
     *
     * @param request Objeto que contiene las credenciales del usuario (nombre de usuario y contraseña).
     * @return AuthResponse con el token JWT generado.
     * @throws AuthenticationException Si las credenciales son inválidas o la autenticación falla.
     * @throws IllegalStateException Si el usuario no se encuentra después de la autenticación.
     */
    public AuthResponse login(LoginRequest request,String sessionId) {
        logger.info("Login attempt for username: {}", request.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException e) {
            logger.warn("Login failed for username: {}", request.getUsername());
            throw e;
        }
        UserDetails user = personRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("User not found after authentication: {}", request.getUsername());
                    return new IllegalStateException("User not found after authentication");
                });

        // Transferir el carrito de Redis a MySQL
        if (sessionId != null && !sessionId.isEmpty()) {
            try {
                Client client = (Client) user;
                cartService.mergeCartOnLogin(sessionId, client);
                logger.info("Carrito transferido para el usuario {} con sessionId: {}", client.getId(), sessionId);
            } catch (Exception e) {
                logger.error("Error al transferir el carrito: {}", e.getMessage());
                // Continuar con el login aunque falle la transferencia del carrito
            }
        } else {
            logger.warn("No se proporcionó X-Session-Id en la solicitud de login");
        }

        String token = jwtService.getToken(user);
        logger.info("Login successful for username: {}", request.getUsername());
        return AuthResponse.builder()
                .token(token)
                .build();
    }

    /**
     * Registra un nuevo usuario en el sistema y genera un token JWT.
     *
     * @param request Objeto que contiene los datos del usuario para el registro (nombre de usuario, contraseña, email, etc.).
     * @return AuthResponse con el token JWT generado.
     * @throws IllegalArgumentException Si el nombre de usuario o el email ya están registrados.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Register attempt for username: {}", request.getUsername());
        if (personRepository.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Registration failed: Username {} already exists", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        if (personRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Registration failed: Email {} already exists", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        Person person = Person.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .role(Role.USER)
                .build();

        personRepository.save(person);

        return AuthResponse.builder()
                .token(jwtService.getToken(person))
                .build();
    }
}