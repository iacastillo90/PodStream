package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Repositories.ProductRepository;
import com.podStream.PodStream.Services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImplement implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(Product product) {
        // Verificar que el producto existe
        findById(product.getId());
        return productRepository.save(product);
    }

    @Override
    public void deleteById(Long id) {
        // Verificar que el producto existe
        findById(id);
        productRepository.deleteById(id);
    }

}