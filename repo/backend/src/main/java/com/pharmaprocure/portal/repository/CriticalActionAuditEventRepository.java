package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.CriticalActionAuditEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriticalActionAuditEventRepository extends JpaRepository<CriticalActionAuditEventEntity, Long> {
    @EntityGraph(attributePaths = {"actorUser"})
    List<CriticalActionAuditEventEntity> findByRequestIdOrderByCreatedAtAsc(Long requestId);
}
