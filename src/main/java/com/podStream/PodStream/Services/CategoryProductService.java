package com.podStream.PodStream.Services;

import com.podStream.PodStream.Models.CategoryProduct;

import java.util.List;

public interface CategoryProductService {

    List<CategoryProduct> findAll();
    CategoryProduct findById(Long id);
    CategoryProduct save(CategoryProduct category);
    CategoryProduct update(CategoryProduct category);
    void deleteById(Long id);

}
