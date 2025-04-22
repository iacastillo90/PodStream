package com.podStream.PodStream.Models.User;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.podStream.PodStream.Models.Address;
import com.podStream.PodStream.Models.Comment;
import com.podStream.PodStream.Models.PurchaseOrder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="user", uniqueConstraints = {@UniqueConstraint(columnNames = {"username"})})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    Integer id;
    @Basic
    @Column(nullable = false)
    String username;
    /**
     * Apellido de la persona.
     */
    @Column(nullable = false)
    String lastname;
    /**
     * Primer nombre de la persona.
     */
    String firstname;
    /**
     * Número de teléfono de la persona.
     */
    private String phone;
    /**
     * País de residencia de la persona.
     */
    String country;
    /**
     * Contraseña de la persona.
     */
    @JsonIgnore
    String password;
    /**
     * Correo electrónico de la persona.
     */
    private String email;
    /**
     * Tipo de usuario (cliente o administrador).
     */
    @Enumerated(EnumType.STRING) 
    Role role;
    /**
     * Relación con las direcciones asociadas a la persona.
     */
    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    @JsonManagedReference(value = "user-address") // Ignorar este lado de la relación
    private Set<Address> adress = new HashSet<>();
    /**
     * Relación con los comentarios realizados por la persona.
     */
    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER)
    private List<Comment> comments = new ArrayList<>();
    /**
     * Relación con las órdenes de compra realizadas por la persona.
     */
    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    @JsonManagedReference(value = "user-purchaseOrder") // Par con @JsonBackReference en PurchaseOrder
    private Set<PurchaseOrder> purchaseOrder = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return List.of(new SimpleGrantedAuthority((role.name())));
    }
    @Override
    public boolean isAccountNonExpired() {
       return true;
    }
    @Override
    public boolean isAccountNonLocked() {
       return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
}
