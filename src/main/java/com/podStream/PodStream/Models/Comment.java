package com.podStream.PodStream.Models;

import com.podStream.PodStream.Models.User.User;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * La entidad Comment representa los comentarios realizados por los usuarios en los productos.
 * Los comentarios pueden tener respuestas asociadas y están vinculados a una persona y un producto.
 *
 * Ejemplo de uso:
 * Comment comment = new Comment("Gran producto", LocalDateTime.now(), respuestas, persona, producto, true);
 */
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    /**
     * Cuerpo del comentario.
     */
    @Column(length = 10000)
    private String body;

    /**
     * Fecha y hora en que se realizó el comentario.
     */
    private LocalDateTime date;

    /**
     * Respuestas asociadas al comentario.
     */
    @OneToMany(mappedBy = "comment")
    private Set<Answers> answers;

    /**
     * Persona que realizó el comentario.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private User person;

    /**
     * Producto sobre el cual se realizó el comentario.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private Product product;

    /**
     * Estado del comentario (activo/inactivo).
     */
    private boolean active;

    /**
     * Constructor vacío necesario para JPA.
     */
    public Comment() {
    }

    /**
     * Constructor que permite crear un comentario con información básica.
     *
     * @param body Cuerpo del comentario.
     * @param date Fecha en que se realizó el comentario.
     * @param answers Conjunto de respuestas asociadas.
     * @param person Persona que realizó el comentario.
     * @param product Producto al que pertenece el comentario.
     * @param active Estado del comentario (activo/inactivo).
     */
    public Comment(String body, LocalDateTime date, Set<Answers> answers, User person, Product product, boolean active) {
        this.body = body;
        this.date = date;
        this.answers = answers;
        this.person = person;
        this.product = product;
        this.active = active;
    }

    // Getters y Setters con Javadoc

    /**
     * Obtiene el ID del comentario.
     *
     * @return ID del comentario.
     */
    public long getId() {
        return id;
    }

    /**
     * Establece el ID del comentario.
     *
     * @param id ID del comentario.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Obtiene el cuerpo del comentario.
     *
     * @return Cuerpo del comentario.
     */
    public String getBody() {
        return body;
    }

    /**
     * Establece el cuerpo del comentario.
     *
     * @param body Cuerpo del comentario.
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Obtiene la fecha en que se realizó el comentario.
     *
     * @return Fecha del comentario.
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Establece la fecha en que se realizó el comentario.
     *
     * @param date Fecha del comentario.
     */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /**
     * Obtiene las respuestas asociadas al comentario.
     *
     * @return Conjunto de respuestas.
     */
    public Set<Answers> getAnswers() {
        return answers;
    }

    /**
     * Establece las respuestas asociadas al comentario.
     *
     * @param answers Conjunto de respuestas.
     */
    public void setAnswers(Set<Answers> answers) {
        this.answers = answers;
    }

    /**
     * Obtiene la persona que realizó el comentario.
     *
     * @return Persona que realizó el comentario.
     */
    public User getPerson() {
        return person;
    }

    /**
     * Establece la persona que realizó el comentario.
     *
     * @param person Persona que realizó el comentario.
     */
    public void setPerson(User person) {
        this.person = person;
    }

    /**
     * Obtiene el producto sobre el cual se realizó el comentario.
     *
     * @return Producto comentado.
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Establece el producto sobre el cual se realizó el comentario.
     *
     * @param product Producto comentado.
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Verifica si el comentario está activo.
     *
     * @return Estado del comentario (activo/inactivo).
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Establece el estado del comentario.
     *
     * @param active Estado del comentario (activo/inactivo).
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
