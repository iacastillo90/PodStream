package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.User.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface AdminRepository extends JpaRepository<Admin, Long> {
    // Métodos personalizados si es necesario
}

