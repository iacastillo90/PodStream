
package com.podStream.PodStream.Services.Listeners;

import com.atlassian.event.api.EventListener;
import com.podStream.PodStream.Exception.RatingUpdateException;
import com.podStream.PodStream.Models.ClientInteraction;
import com.podStream.PodStream.Models.InteractionType;
import com.podStream.PodStream.Models.ProductRating;
import com.podStream.PodStream.Repositories.Jpa.ProductRatingRepository;
import com.podStream.PodStream.Services.Events.ClientInteractionEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


/**
 * Listener que procesa eventos de interacción de usuarios para actualizar o crear calificaciones de productos.
 * Se encarga de inferir calificaciones basadas en el tipo de interacción (vista, compra, etc.) y persistirlas en la base de datos.
 *
 * <p>Este componente utiliza {@link ProductRatingRepository} para gestionar las calificaciones de productos y responde a eventos de tipo
 * {@link ClientInteractionEvent}. Las calificaciones se actualizan solo si la nueva interacción implica una calificación más alta que la existente.
 *
 * @author [Tu Nombre o Equipo PodStream]
 * @since 0.0.1-SNAPSHOT
 */

@Component
@RequiredArgsConstructor
public class RatingUpdateListener {

    private final ProductRatingRepository productRatingRepository;

    private static final Logger logger = LoggerFactory.getLogger(RatingUpdateListener.class);


/**
     * Maneja eventos de interacción de usuarios para inferir y actualizar calificaciones de productos.
     * <p>Recibe un evento {@link ClientInteractionEvent}, calcula una calificación implícita basada en el tipo de interacción
     * (por ejemplo, 5.0 para compras, 1.0 para vistas) y actualiza o crea una entrada en {@link ProductRating}.
     *
     * @param event El evento de interacción del usuario, que contiene la información de la interacción.
     * @throws IllegalArgumentException Si la interacción es nula o contiene datos inválidos.
     * @throws EntityNotFoundException Si no se encuentra el cliente o producto asociado.
     */

    @EventListener
    @Transactional
    public void handleUserInteraction(ClientInteractionEvent event) {
        if (event == null || event.getInteraction() == null) {
            logger.error("Evento de interacción nulo recibido");
            throw new IllegalArgumentException("El evento de interacción no puede ser nulo");
        }

        ClientInteraction interaction = event.getInteraction();
        logger.info("Procesando evento de interacción para cliente {} y producto {}",
                interaction.getClient().getId(), interaction.getProduct().getId());

        // Validar cliente y producto
        if (interaction.getClient() == null || interaction.getProduct() == null) {
            logger.error("Interacción inválida: cliente o producto nulo");
            throw new EntityNotFoundException("Cliente o producto no encontrado en la interacción");
        }

        // Inferir calificación basada en el tipo de interacción
        Integer inferredRating = (int) inferRatingFromInteraction(interaction);

        // Buscar calificación existente
        Optional<ProductRating> existingRating = productRatingRepository.findByClientIdAndProductId(
                interaction.getClient().getId(), interaction.getProduct().getId());

        ProductRating productRating;
        if (existingRating.isPresent()) {
            productRating = existingRating.get();
            // Actualizar solo si la nueva calificación es más alta
            if (inferredRating > productRating.getRating()) {
                productRating.setRating(inferredRating);
                productRating.setCreatedAt(LocalDateTime.now());
                logger.debug("Calificación actualizada para cliente {} y producto {} a {}",
                        interaction.getClient().getId(), interaction.getProduct().getId(), inferredRating);
            } else {
                logger.debug("No se actualizó la calificación para cliente {} y producto {}. Calificación existente {} es mayor o igual",
                        interaction.getClient().getId(), interaction.getProduct().getId(), productRating.getRating());
                return; // Evitar guardar si no hay cambios
            }
        } else {
            productRating = new ProductRating();
            productRating.setClient(interaction.getClient());
            productRating.setProduct(interaction.getProduct());
            productRating.setRating(inferredRating);
            productRating.setCreatedAt(LocalDateTime.now());
            logger.debug("Nueva calificación creada para cliente {} y producto {} con valor {}",
                    interaction.getClient().getId(), interaction.getProduct().getId(), inferredRating);
        }

        // Guardar la calificación
        try {
            productRatingRepository.save(productRating);
            logger.info("Calificación guardada exitosamente para cliente {} y producto {}",
                    interaction.getClient().getId(), interaction.getProduct().getId());
        } catch (Exception e) {
            logger.error("Error al guardar la calificación para cliente {} y producto {}: {}",
                    interaction.getClient().getId(), interaction.getProduct().getId(), e.getMessage());
            throw new RatingUpdateException("Error al persistir la calificación: " + e.getMessage(), e);
        }
    }


/**
     * Calcula una calificación implícita basada en el tipo de interacción del usuario.
     * <p>Los tipos de interacción están definidos en {@link InteractionType} y cada uno tiene un peso asociado:
     * <ul>
     *     <li>Vista (VIEW): 1.0</li>
     *     <li>Añadir al carrito (ADD_TO_CART): 3.0</li>
     *     <li>Compra (PURCHASE): 5.0</li>
     *     <li>Calificación explícita (RATING): Valor proporcionado por el campo quantity</li>
     *     <li>Clic (CLICK): 2.0</li>
     *     <li>Búsqueda (SEARCH): 0.5</li>
     * </ul>
     *
     * @param interaction La interacción del usuario con un producto.
     * @return La calificación implícita calculada.
     * @throws IllegalArgumentException Si el tipo de interacción es inválido o el quantity es nulo para RATING.
     */

    private double inferRatingFromInteraction(ClientInteraction interaction) {
        InteractionType interactionType = interaction.getInteractionType();
        if (interactionType == null) {
            logger.warn("Tipo de interacción nulo recibido");
            return 0.0;
        }

        switch (interactionType) {
            case VIEW:
                return 1.0;
            case ADD_TO_CART:
                return 3.0;
            case PURCHASE:
                return 5.0;
            case RATING:
                Integer quantity = interaction.getQuantity();
                if (quantity == null) {
                    logger.error("Calificación explícita sin valor en quantity");
                    throw new IllegalArgumentException("El campo quantity es requerido para interacciones de tipo RATING");
                }
                double rating = quantity.doubleValue();
                if (rating < 0 || rating > 5) {
                    logger.warn("Calificación explícita inválida: {}", rating);
                    throw new IllegalArgumentException("La calificación debe estar entre 0 y 5");
                }
                return rating;
            case CLICK:
                return 2.0;
            case SEARCH:
                return 0.5;
            default:
                logger.warn("Tipo de interacción no soportado: {}", interactionType);
                return 0.0;
        }
    }

/**
     * Procesa un evento de interacción de usuario para actualizar o crear una calificación de producto.
     *
     * @param event El evento que contiene la interacción del usuario.
     */

    @EventListener
    public void handleClientInteractionEvent(ClientInteractionEvent event) {
        ClientInteraction interaction = event.getInteraction();
        logger.info("Processing interaction event for user {} and product {}",
                interaction.getClient().getId(), interaction.getProduct().getId());

        Double rating = calculateRating(interaction.getInteractionType(), interaction.getQuantity());
        if (rating == null) {
            logger.debug("No rating generated for interaction type {}", interaction.getInteractionType());
            return;
        }

        Optional<ProductRating> existingRating = productRatingRepository
                .findByClientIdAndProductId(interaction.getClient().getId(), interaction.getProduct().getId());

        ProductRating productRating;
        if (existingRating.isPresent()) {
            productRating = existingRating.get();
            productRating.setRating(rating.intValue());
            // No actualizamos el timestamp, ya que @CreatedDate lo gestiona
        } else {
            productRating = new ProductRating( );
        }

        productRatingRepository.save(productRating);
        logger.info("Saved product rating for user {} and product {} with rating {}",
                interaction.getClient().getId(), interaction.getProduct().getId(), rating);
    }


/**
     * Calcula la calificación basada en el tipo de interacción y la cantidad.
     *
     * @param type El tipo de interacción (VIEW, ADD_TO_CART, PURCHASE, RATING).
     * @param quantity La cantidad asociada a la interacción (por ejemplo, número de ítems comprados).
     * @return La calificación calculada, o null si no se genera una calificación.
     */

    private Double calculateRating(InteractionType type, Integer quantity) {
        switch (type) {
            case VIEW:
                return 3.0; // Vista genera una calificación media
            case ADD_TO_CART:
                return 4.0; // Añadir al carrito indica interés
            case PURCHASE:
                return quantity != null && quantity > 0 ? 5.0 : 4.5; // Compra genera alta calificación
            case RATING:
                // Suponemos que el rating explícito viene de otro campo o lógica
                return null; // Implementar según necesidades
            default:
                return null;
        }
    }
}

