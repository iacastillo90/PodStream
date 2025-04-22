package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
}
