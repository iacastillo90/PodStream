package com.podStream.PodStream.Models.User;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.podStream.PodStream.Models.Address;
import com.podStream.PodStream.Models.Comment;
import com.podStream.PodStream.Models.PurchaseOrder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "client")
public class Client extends User {

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
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    @JsonManagedReference(value = "client-purchaseOrder")
    private Set<PurchaseOrder> purchaseOrders = new HashSet<>();
}

