package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.DocumentArchiveRecordEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentArchiveRecordRepository extends JpaRepository<DocumentArchiveRecordEntity, Long> {
    @EntityGraph(attributePaths = {"document", "version", "archivedBy"})
    List<DocumentArchiveRecordEntity> findAllByOrderByArchivedAtDesc();
}
