package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.CheckInAttachmentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInAttachmentRepository extends JpaRepository<CheckInAttachmentEntity, Long> {
    List<CheckInAttachmentEntity> findByCheckInIdOrderByCreatedAtDesc(Long checkInId);
}
