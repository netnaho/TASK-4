package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.ProcurementOrderItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcurementOrderItemRepository extends JpaRepository<ProcurementOrderItemEntity, Long> {
    List<ProcurementOrderItemEntity> findByOrderId(Long orderId);
}
