package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.DocumentTemplateEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplateEntity, Long> {
    @EntityGraph(attributePaths = {"documentType"})
    List<DocumentTemplateEntity> findAllByOrderByCreatedAtDesc();
}
