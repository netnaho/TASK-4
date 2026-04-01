package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.OrderPaymentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPaymentRepository extends JpaRepository<OrderPaymentEntity, Long> {
    Optional<OrderPaymentEntity> findByOrderId(Long orderId);
}
