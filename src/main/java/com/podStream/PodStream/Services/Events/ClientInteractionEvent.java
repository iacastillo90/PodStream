
package com.podStream.PodStream.Services.Events;

import com.podStream.PodStream.Models.ClientInteraction;
import org.springframework.context.ApplicationEvent;

import java.util.Objects;


/**
 * Evento de Spring que representa una interacción de un usuario con un producto en la aplicación PodStream.
 * <p>Este evento es disparado cuando un usuario realiza una acción como ver, comprar o calificar un producto,
 * y es consumido por listeners como {@link com.podStream.PodStream.Services.Listeners.RatingUpdateListener}
 * para actualizar calificaciones de productos o alimentar el motor de recomendaciones.
 *
 * @author [Tu Nombre o Equipo PodStream]
 * @since 0.0.1-SNAPSHOT
 */

public class ClientInteractionEvent extends ApplicationEvent {

    private final ClientInteraction interaction;


/**
     * Crea un nuevo evento de interacción de usuario.
     *
     * @param source La fuente que dispara el evento (generalmente un componente de Spring).
     * @param interaction La interacción del usuario con un producto, que no puede ser nula.
     * @throws IllegalArgumentException Si la interacción es nula.
     */

    public ClientInteractionEvent(Object source, ClientInteraction interaction) {
        super(source);
        Objects.requireNonNull(interaction, "La interacción no puede ser nula");
        this.interaction = interaction;
    }


/**
     * Obtiene la interacción asociada al evento.
     *
     * @return La interacción del usuario con un producto.
     */

    public ClientInteraction getInteraction() {
        return interaction;
    }
}
