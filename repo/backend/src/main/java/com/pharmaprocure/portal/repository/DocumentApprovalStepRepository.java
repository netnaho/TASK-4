package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.DocumentApprovalStepEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentApprovalStepRepository extends JpaRepository<DocumentApprovalStepEntity, Long> {
    List<DocumentApprovalStepEntity> findByRouteIdOrderByStepOrderAsc(Long routeId);
}
