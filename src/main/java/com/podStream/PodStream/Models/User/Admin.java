package com.podStream.PodStream.Models.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "admin")
public class Admin extends User {

    @Column(name = "access_level")
    private String accessLevel;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "notes")
    private String notes;
}

