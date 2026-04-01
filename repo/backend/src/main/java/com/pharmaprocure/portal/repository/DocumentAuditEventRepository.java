package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.DocumentAuditEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentAuditEventRepository extends JpaRepository<DocumentAuditEventEntity, Long> {
    @EntityGraph(attributePaths = {"actorUser"})
    List<DocumentAuditEventEntity> findByDocumentIdOrderByCreatedAtDesc(Long documentId);
}
