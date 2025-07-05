package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Models.CategoryProduct;
import com.podStream.PodStream.Repositories.CategoryProductRepository;
import com.podStream.PodStream.Repositories.ProductRepository;
import com.podStream.PodStream.Services.CategoryProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryProductServiceImplement implements CategoryProductService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryProductServiceImplement.class);

    private final CategoryProductRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public List<CategoryProduct> findAll() {
        logger.info("Fetching all categories");
        return categoryRepository.findAll();
    }

    @Override
    public CategoryProduct findById(Long id) {
        logger.info("Fetching category with id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }

    @Override
    public CategoryProduct save(CategoryProduct category) {
        logger.info("Saving category: {}", category.getName());
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new IllegalArgumentException("Category name already exists: " + category.getName());
        }
        return categoryRepository.save(category);
    }

    @Override
    public CategoryProduct update(CategoryProduct category) {
        logger.info("Updating category with id: {}", category.getId());
        CategoryProduct existing = findById(category.getId());
        if (!existing.getName().equals(category.getName()) &&
                categoryRepository.findByName(category.getName()).isPresent()) {
            throw new IllegalArgumentException("Category name already exists: " + category.getName());
        }
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        return categoryRepository.save(existing);
    }

    @Override
    public void deleteById(Long categoryId) {
        logger.info("Deleting category with id: {}", categoryId);
        if (productRepository.existsByCategoryId(categoryId)) {
            throw new IllegalStateException("Cannot delete category with associated products");
        }
        categoryRepository.deleteById(categoryId);
    }


}
