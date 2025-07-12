package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.Details;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailsRepository extends JpaRepository<Details, Long> {
    List<Details> findByPurchaseOrderIdAndActiveTrue(Long purchaseOrderId);
    List<Details> findByProductIdAndActiveTrue(Long productId);
}
