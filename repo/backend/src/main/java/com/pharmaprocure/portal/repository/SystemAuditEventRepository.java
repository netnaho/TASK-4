package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.SystemAuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemAuditEventRepository extends JpaRepository<SystemAuditEventEntity, Long> {
}
