package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponseDTO {

    private Long id;

    private String productName;


    private Double score;

    private String category;
    private String image;

}

