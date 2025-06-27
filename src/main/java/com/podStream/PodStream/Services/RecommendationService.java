package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.RecommendationResponseDTO;
import java.util.List;

public interface RecommendationService {

    List<RecommendationResponseDTO> getRecommendationsForUser(Long userId, int howMany);

//    List<RecommendationResponseDTO> getPopularProducts(int howMany);


    List<RecommendationResponseDTO> getPopularProducts(int howMany);

    List<RecommendationResponseDTO> getContentBasedRecommendations(Long productId, int howMany);

}
