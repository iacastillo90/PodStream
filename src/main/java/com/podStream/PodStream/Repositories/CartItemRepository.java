package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface CartItemRepository extends JpaRepository <CartItem, Long> {
}
