package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.OrderReturnEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderReturnRepository extends JpaRepository<OrderReturnEntity, Long> {
    @EntityGraph(attributePaths = {"items", "items.orderItem"})
    List<OrderReturnEntity> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
