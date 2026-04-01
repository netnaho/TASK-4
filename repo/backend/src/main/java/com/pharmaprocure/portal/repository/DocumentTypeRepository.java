package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.DocumentTypeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTypeRepository extends JpaRepository<DocumentTypeEntity, Long> {
    Optional<DocumentTypeEntity> findByCode(String code);
}
