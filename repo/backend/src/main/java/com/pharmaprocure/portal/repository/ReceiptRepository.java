package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.ReceiptEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<ReceiptEntity, Long> {
    @EntityGraph(attributePaths = {"items", "items.orderItem"})
    List<ReceiptEntity> findByOrderIdOrderByReceivedAtAsc(Long orderId);
}
