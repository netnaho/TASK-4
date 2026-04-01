package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.AfterSalesCaseEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AfterSalesCaseRepository extends JpaRepository<AfterSalesCaseEntity, Long> {
    List<AfterSalesCaseEntity> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
