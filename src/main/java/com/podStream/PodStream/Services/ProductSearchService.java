package com.podStream.PodStream.Services;

import com.podStream.PodStream.Models.CategoryProduct;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Models.ProductDocument;
import com.podStream.PodStream.Repositories.ProductRepository;
import com.podStream.PodStream.Repositories.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSearchService {
    private static final Logger logger = LoggerFactory.getLogger(ProductSearchService.class);

    @Autowired
    private ProductSearchRepository productSearchRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<ProductDocument> searchProducts(String query) {
        try {
            logger.info("Buscando productos con query: {}", query);
            return productSearchRepository.findByNameContainingOrDescriptionContaining(query, query);
        } catch (Exception e) {
            logger.error("Error al buscar en Elasticsearch, usando MySQL: {}", e.getMessage());
            // Fallback to MySQL
            List<Product> products = productRepository.findAll().stream()
                    .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()) ||
                            p.getDescription().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            return products.stream().map(this::convertToDocument).collect(Collectors.toList());
        }
    }

    public List<ProductDocument> filterProducts(Long categoryId, Double minPrice, Double maxPrice) {
        try {
            logger.info("Filtrando productos por categoría ID: {}, precio: {}-{}", categoryId, minPrice, maxPrice);
            return productSearchRepository.findByCategoryIdAndPriceBetween(categoryId, minPrice, maxPrice);
        } catch (Exception e) {
            logger.error("Error al filtrar en Elasticsearch, usando MySQL: {}", e.getMessage());
            List<Product> products = productRepository.findAll().stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId) &&
                            p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
                    .collect(Collectors.toList());
            return products.stream().map(this::convertToDocument).collect(Collectors.toList());
        }
    }

    public void syncProduct(Product product) {
        try {
            ProductDocument document = new ProductDocument();
            document.setId(product.getId());
            document.setName(product.getName());
            document.setDescription(product.getDescription());
            document.setPrice(product.getPrice());
            document.setStock(product.getStock());
            document.setCategory(product.getCategory());
            productSearchRepository.save(document);
            logger.info("Producto {} sincronizado con Elasticsearch", product.getId());
        } catch (Exception e) {
            logger.error("Error al sincronizar producto {} con Elasticsearch: {}", product.getId(), e.getMessage());
        }
    }

    private ProductDocument convertToDocument(Product product) {
        ProductDocument document = new ProductDocument();
        document.setId(product.getId());
        document.setName(product.getName());
        document.setDescription(product.getDescription());
        document.setPrice(product.getPrice());
        document.setStock(product.getStock());
        document.setCategory(product.getCategory());
        return document;
    }
}
