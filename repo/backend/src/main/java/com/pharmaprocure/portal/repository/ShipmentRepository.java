package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.ShipmentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {
    @EntityGraph(attributePaths = {"items", "items.orderItem"})
    List<ShipmentEntity> findByOrderIdOrderByShippedAtAsc(Long orderId);
}
