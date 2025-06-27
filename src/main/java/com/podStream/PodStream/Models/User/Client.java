package com.podStream.PodStream.Models.User;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.podStream.PodStream.Models.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("CLIENT")
public class Client extends Person {

    /**
     * Número de teléfono de la persona.
     */
    private String phone;

    /**
     * País de residencia de la persona.
     */
    String country;

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

}

