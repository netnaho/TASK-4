package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.OrderStatusHistoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistoryEntity, Long> {
    List<OrderStatusHistoryEntity> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
