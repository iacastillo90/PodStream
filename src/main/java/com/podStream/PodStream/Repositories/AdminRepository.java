package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.User.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    // Métodos personalizados si es necesario
}

