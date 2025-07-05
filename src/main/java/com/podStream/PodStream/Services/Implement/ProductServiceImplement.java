package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Controllers.NotificationController;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Models.ProductDocument;
import com.podStream.PodStream.Repositories.ProductRepository;
import com.podStream.PodStream.Services.ProductSearchService;
import com.podStream.PodStream.Services.ProductService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductServiceImplement implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final String PRODUCT_CACHE_KEY = "product:";

    private final ProductRepository productRepository;

    private final ProductSearchService productSearchService;

    @Autowired
    private
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private NotificationController notificationController;

    @Override
    @Transactional
    public Product updateStock(Long productId, Integer newStock, String updatedBy) {
        if (newStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productId));

        int oldStock = product.getStock();
        product.setStock(newStock);
        productRepository.save(product);

        // Sincronizar con Elasticsearch (maneja errores internamente)
        productSearchService.syncProduct(product);

        // Actualizar caché en Redis
        redisTemplate.opsForValue().set(PRODUCT_CACHE_KEY + productId, product, 1, TimeUnit.HOURS);
        logger.info("Stock del producto {} actualizado de {} a {} por {}", productId, oldStock, newStock, updatedBy);
        meterRegistry.gauge("inventory.stock", product.getStock());
        if (newStock <= 10) {
            meterRegistry.counter("inventory.low_stock", "productId", productId.toString()).increment();
        }

        notificationController.notifyStockUpdate(productId, newStock);

        return product;
    }

    @Override
    public Product getProduct(Long productId) {
        // Intentar obtener del caché
        String cacheKey = PRODUCT_CACHE_KEY + productId;
        Product cachedProduct = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProduct != null) {
            logger.info("Producto {} obtenido desde caché", productId);
            meterRegistry.counter("cache.hits", "type", "product").increment();
            return cachedProduct;
        }

        // Obtener de MySQL y guardar en caché
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productId));
        redisTemplate.opsForValue().set(cacheKey, product, 1, TimeUnit.HOURS);
        logger.info("Producto {} obtenido desde MySQL y cacheado", productId);
        meterRegistry.counter("cache.misses", "type", "product").increment();
        return product;
    }

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