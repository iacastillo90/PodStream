package com.podStream.PodStream.Models.User;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.podStream.PodStream.Models.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("CLIENT")
public class Client extends Person {

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Customer RUT cannot be empty")
    @Pattern(regexp = "\\d{1,2}\\.\\d{3}\\.\\d{3}-[0-9kK]", message = "Invalid customer RUT")
    private String customerRut;

    @NotBlank(message = "Country is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a valid ISO 3166-1 alpha-2 code")
    private String country;

    private Boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    @JsonManagedReference(value = "client-address")
    private Set<Address> addresses = new HashSet<>();

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private List<Answers> answers = new ArrayList<>();

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    @JsonManagedReference(value = "client-purchaseOrder")
    private Set<PurchaseOrder> purchaseOrders = new HashSet<>();

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private Set<ClientInteraction> interactions = new HashSet<>();

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private Set<ProductRating> productRatings = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private Set<SupportTicket> supportTickets = new HashSet<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Cart> carts = new HashSet<>();

    public Client( String username, String firstname, String lastname, String password, String email, String phone, String country, Boolean active) {
        super(username, firstname, lastname, password, email, Role.CLIENT);
        this.phone = phone;
        this.country = country;
        this.active = active;
    }


    public static boolean isActive(@NotNull SupportTicket supportTicket) {
        return supportTicket.getStatus() != OrderStatus.CLOSED;

    }
}

