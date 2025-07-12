package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.DetailsDTO;
import com.podStream.PodStream.DTOS.DetailsRequestDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface DetailsService {
    DetailsDTO createDetails(DetailsRequestDTO request, Authentication authentication);
    DetailsDTO getDetails(Long id, Authentication authentication);
    List<DetailsDTO> getDetailsByPurchaseOrder(Long purchaseOrderId, Authentication authentication);
    List<DetailsDTO> getDetailsByProduct(Long productId, Authentication authentication);
    DetailsDTO updateDetails(Long id, DetailsRequestDTO request, Authentication authentication);
    void deleteDetails(Long id, Authentication authentication);
}
