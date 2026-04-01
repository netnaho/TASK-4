package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.CheckInRevisionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRevisionRepository extends JpaRepository<CheckInRevisionEntity, Long> {
    @EntityGraph(attributePaths = {"editedBy"})
    List<CheckInRevisionEntity> findByCheckInIdOrderByRevisionNumberDesc(Long checkInId);
}
