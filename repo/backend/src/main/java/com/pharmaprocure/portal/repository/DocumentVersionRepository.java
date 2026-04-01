package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.DocumentVersionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersionEntity, Long> {
    List<DocumentVersionEntity> findByDocumentIdOrderByVersionNumberDesc(Long documentId);
}
