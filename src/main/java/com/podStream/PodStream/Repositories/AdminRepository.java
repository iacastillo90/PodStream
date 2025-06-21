package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.User.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    // Métodos personalizados si es necesario
}

