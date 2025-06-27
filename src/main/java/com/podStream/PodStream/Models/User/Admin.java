package com.podStream.PodStream.Models.User;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends Person {

    @Column(name = "access_level")
    private String accessLevel;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "notes")
    private String notes;
}

