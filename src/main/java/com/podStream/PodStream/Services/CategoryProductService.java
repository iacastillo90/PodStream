package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.CategoryProductDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface CategoryProductService {


    List<CategoryProductDTO> findAll(Authentication authentication);
    CategoryProductDTO findById(Long id, Authentication authentication);
    CategoryProductDTO save(CategoryProductDTO categoryDTO, Authentication authentication);
    CategoryProductDTO update(Long id, CategoryProductDTO categoryDTO, Authentication authentication);
    void deleteById(Long id, Authentication authentication);

}
