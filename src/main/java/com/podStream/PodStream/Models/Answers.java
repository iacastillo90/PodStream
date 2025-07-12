package com.podStream.PodStream.Models;

import com.podStream.PodStream.Models.User.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * La entidad Answers representa una respuesta a un comentario en la plataforma.
 * Almacena la respuesta hecha por un usuario a un comentario específico.
 *
 * Ejemplo de uso:
 * Answers answer = new Answers(1L, "Esta es una respuesta", "user123", comment, person, LocalDateTime.now(), true);
 */
@Entity
@Table(name = "answers")
@Data
@Document(indexName = "answers")
@EntityListeners(AuditingEntityListener.class)
public class Answers {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    /**
     * Cuerpo de la respuesta.
     */
    @NotBlank(message = "Body is required")
    @Size(max = 10000, message = "Body must not exceed 10000 characters")
    @Column(length = 10000)
    private String body;

    /**
     * Nombre de usuario que realizó la respuesta.
     */
    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must not exceed 50 characters")
    private String userName;

    /**
     * Comentario asociado a la respuesta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    /**
     * Cliente que realizó la respuesta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Fecha de creación de la respuesta.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha de última actualización de la respuesta.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Indica si la respuesta está activa.
     */
    private boolean active = true;


}
