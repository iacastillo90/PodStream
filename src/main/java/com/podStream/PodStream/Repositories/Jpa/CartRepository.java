package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository <Cart, Long>  {

    Optional<Cart> findByClientId(Long clientId);
    Optional<Cart> findBySessionId(String sessionId);
    boolean existsByIdAndClientId(Long id, Long clientId);

}
