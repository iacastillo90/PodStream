package com.podStream.PodStream.Models;

import com.podStream.PodStream.Models.User.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un comentario realizado por un cliente sobre un producto en PodStream.
 */
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @NotBlank(message = "El cuerpo del comentario no puede estar vacío")
    @Column(length = 10000, nullable = false)
    private String body;

    @NotNull(message = "La fecha del comentario es obligatoria")
    @CreatedDate
    @Column(name = "date", nullable = false, updatable = false)
    private LocalDateTime date;

    @OneToMany(mappedBy = "comment", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Answers> answers = new HashSet<>();

    @NotNull(message = "El cliente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "active")
    private boolean active = true;

    public Comment() {
    }

    public Comment(String body, LocalDateTime date, Set<Answers> answers, Client client, Product product, boolean active) {
        this.body = body;
        this.date = date;
        this.answers = answers;
        this.client = client;
        this.product = product;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Set<Answers> getAnswers() {
        return answers;
    }

    public void setAnswers(Set<Answers> answers) {
        this.answers = answers;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}