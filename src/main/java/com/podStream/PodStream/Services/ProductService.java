package com.podStream.PodStream.Services;

import com.podStream.PodStream.Models.Product;

import java.util.List;

public interface ProductService {
    List<Product> findAll();
    Product findById(Long id);
    Product save(Product product);
    Product update(Product product);
    void deleteById(Long id);
    Product updateStock(Long productId, Integer newStock, String updatedBy);
    Product getProduct(Long productId);

}