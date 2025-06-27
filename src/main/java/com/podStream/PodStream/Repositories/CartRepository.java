package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface CartRepository extends JpaRepository <Cart, Long>  {

    Optional<Cart> findByClientId(Long clientId);
    Optional<Cart> findBySessionId(String sessionId);

}
