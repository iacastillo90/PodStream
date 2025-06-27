package com.podStream.PodStream.Configurations.JpaAudit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;


/**
 * Configuración para habilitar la auditoría JPA en el proyecto PodStream.
 * <p>Activa el soporte para anotaciones como {@code @CreatedDate} y {@code @LastModifiedDate}
 * en las entidades, permitiendo la gestión automática de campos de auditoría como fechas de creación
 * y modificación.
 *
 * @author [Tu Nombre o Equipo PodStream]
 * @since 1.0.0-SNAPSHOT
 */


@EnableJpaAuditing
public class JpaAuditingConfig {

/**
     * Proporciona el usuario o sistema actual para las anotaciones {@code @CreatedBy} y {@code @LastModifiedBy}.
     * <p>Este bean es opcional y puede personalizarse para devolver el ID del usuario autenticado
     * o un identificador del sistema. Actualmente devuelve un valor fijo para propósitos de demostración.
     *
     * @return Un AuditorAware que proporciona el identificador del auditor.
     */

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("PodStreamSystem"); // Reemplazar con lógica de autenticación real, por ejemplo, SecurityContextHolder
    }

}
