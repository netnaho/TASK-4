package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.OrderReviewEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderReviewRepository extends JpaRepository<OrderReviewEntity, Long> {
    Optional<OrderReviewEntity> findByOrderId(Long orderId);
}
