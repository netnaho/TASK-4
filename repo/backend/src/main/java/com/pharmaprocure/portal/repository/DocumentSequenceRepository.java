package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.DocumentSequenceEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface DocumentSequenceRepository extends JpaRepository<DocumentSequenceEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DocumentSequenceEntity> findByTypeCodeAndSequenceYear(String typeCode, int sequenceYear);
}
