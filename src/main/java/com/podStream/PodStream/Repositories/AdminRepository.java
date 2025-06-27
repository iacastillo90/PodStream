package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.User.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@RepositoryRestResource
public interface AdminRepository extends JpaRepository<Admin, Long> {
    // Métodos personalizados si es necesario
}

