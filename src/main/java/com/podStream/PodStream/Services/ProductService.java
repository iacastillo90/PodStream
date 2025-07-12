package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.ProductDTO;
import com.podStream.PodStream.Models.Product;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Interfaz para gestionar productos en PodStream.
 */
public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO, Authentication authentication);
    ProductDTO getProduct(Long id);
    List<ProductDTO> getAllProducts();
    List<ProductDTO> getProductsByName(String name);
    List<ProductDTO> getProductsByCategory(Long categoryId);
    List<ProductDTO> getProductsByPriceRange(double minPrice, double maxPrice);
    List<ProductDTO> getTopPopularProducts(int limit);
    ProductDTO updateProduct(Long id, ProductDTO productDTO, Authentication authentication);
    ProductDTO updateStock(Long id, Integer newStock, String updatedBy, Authentication authentication);
    void deleteProduct(Long id, Authentication authentication);
    void addRating(Long id, int rating, Long clientId, Authentication authentication);

}