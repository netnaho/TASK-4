package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.CriticalActionApprovalEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriticalActionApprovalRepository extends JpaRepository<CriticalActionApprovalEntity, Long> {
    @EntityGraph(attributePaths = {"approverUser"})
    List<CriticalActionApprovalEntity> findByRequestIdOrderByCreatedAtAsc(Long requestId);

    Optional<CriticalActionApprovalEntity> findByRequestIdAndApproverUserId(Long requestId, Long approverUserId);
}
