package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.User.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    // Métodos personalizados si es necesario
}

