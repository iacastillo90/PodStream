package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
