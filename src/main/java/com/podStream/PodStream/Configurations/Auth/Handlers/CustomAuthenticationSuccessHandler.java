package com.podStream.PodStream.Configurations.Auth.Handlers;

import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Services.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Autowired
    private CartService cartService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Obtener el sessionId del header
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || sessionId.isEmpty()) {
            logger.warn("No X-Session-Id provided in authentication request");
            // Fallback: usar el ID de la sesión HTTP si está disponible
            sessionId = request.getSession().getId();
        }

        // Obtener el usuario autenticado
        Client client = (Client) authentication.getPrincipal();
        logger.info("Usuario autenticado: {}, sessionId: {}", client.getId(), sessionId);

        // Transferir el carrito de Redis a MySQL
        try {
            cartService.mergeCartOnLogin(sessionId, client);
            logger.info("Carrito transferido para el usuario {}", client.getId());
        } catch (Exception e) {
            logger.error("Error al transferir el carrito para el usuario {}: {}", client.getId(), e.getMessage());
        }

        // Responder con un mensaje de éxito o redirigir
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"message\": \"Login successful, cart merged\"}");
        // Opcional: Redirigir al frontend (e.g., response.sendRedirect("/cart"));
    }
}
