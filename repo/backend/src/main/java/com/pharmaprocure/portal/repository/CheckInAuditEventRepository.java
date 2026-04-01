package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.CheckInAuditEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInAuditEventRepository extends JpaRepository<CheckInAuditEventEntity, Long> {
    @EntityGraph(attributePaths = {"actorUser"})
    List<CheckInAuditEventEntity> findByCheckInIdOrderByCreatedAtDesc(Long checkInId);
}
