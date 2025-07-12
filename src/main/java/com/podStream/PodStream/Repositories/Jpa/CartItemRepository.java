package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartIdAndActiveTrue(Long cartId);
    Optional<CartItem> findByCartIdAndProductIdAndActiveTrue(Long cartId, Long productId);
}