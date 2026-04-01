package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.DocumentApprovalRouteEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentApprovalRouteRepository extends JpaRepository<DocumentApprovalRouteEntity, Long> {
    @EntityGraph(attributePaths = {"steps", "steps.approverUser", "version"})
    Optional<DocumentApprovalRouteEntity> findByDocumentId(Long documentId);
}
