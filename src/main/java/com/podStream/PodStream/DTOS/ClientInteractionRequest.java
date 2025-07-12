
package com.podStream.PodStream.DTOS;


import com.podStream.PodStream.Models.InteractionType;
import com.podStream.PodStream.Services.Events.ClientInteractionEvent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * DTO para recibir solicitudes de interacción de usuarios con productos en la aplicación PodStream.
 * <p>Este objeto se utiliza en el endpoint {@code /api/recommendations/interactions} para registrar
 * acciones como vistas, compras o calificaciones de productos. Los datos proporcionados se convierten
 * en una entidad {@link com.podStream.PodStream.Models.ClientInteraction} para su persistencia y
 * posterior procesamiento en eventos como {@link ClientInteractionEvent}.
 *
 * @author PodStream
 * @since 0.0.1-SNAPSHOT
 **/

@Data
public class ClientInteractionRequest {


/**
     * Identificador del cliente que realiza la interacción.
     */

    @NotNull(message = "El ID del cliente es obligatorio")
    @Min(value = 1, message = "El ID del cliente debe ser mayor que 0")
    private Long userId;


/**
     * Identificador del producto con el que se interactúa.
     */

    @NotNull(message = "El ID del producto es obligatorio")
    @Min(value = 1, message = "El ID del producto debe ser mayor que 0")
    private Long productId;


/**
     * Tipo de interacción realizada (por ejemplo, VIEW, PURCHASE, RATING).
     */

    @NotNull(message = "El tipo de interacción es obligatorio")
    private InteractionType interactionType;


/**
     * Cantidad asociada a la interacción (por ejemplo, calificación explícita para RATING o número de ítems para PURCHASE).
     * <p>Opcional, pero requerido para interacciones de tipo RATING.
     */

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer quantity;
/**
     * Identificador de la sesión del usuario para rastreo de interacciones.
     * <p>Opcional.
     */

    private String sessionId;
}
