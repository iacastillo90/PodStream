package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.User.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    // Métodos personalizados si es necesario
}

