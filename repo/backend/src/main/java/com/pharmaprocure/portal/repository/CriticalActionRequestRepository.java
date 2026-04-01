package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.CriticalActionRequestEntity;
import com.pharmaprocure.portal.enums.CriticalActionRequestType;
import com.pharmaprocure.portal.enums.CriticalActionStatus;
import com.pharmaprocure.portal.enums.CriticalActionTargetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CriticalActionRequestRepository extends JpaRepository<CriticalActionRequestEntity, Long> {
    @EntityGraph(attributePaths = {"requestedBy"})
    List<CriticalActionRequestEntity> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"requestedBy"})
    List<CriticalActionRequestEntity> findByRequestedByIdOrderByCreatedAtDesc(Long requestedById);

    @EntityGraph(attributePaths = {"requestedBy"})
    List<CriticalActionRequestEntity> findByRequestedByOrganizationCodeOrderByCreatedAtDesc(String organizationCode);

    @EntityGraph(attributePaths = {"requestedBy"})
    Optional<CriticalActionRequestEntity> findByRequestTypeAndTargetTypeAndTargetIdAndStatusIn(
        CriticalActionRequestType requestType,
        CriticalActionTargetType targetType,
        Long targetId,
        List<CriticalActionStatus> statuses
    );

    @EntityGraph(attributePaths = {"requestedBy"})
    @Query("SELECT r FROM CriticalActionRequestEntity r WHERE "
        + "(:requestedById IS NULL OR r.requestedBy.id = :requestedById) AND "
        + "(:orgCode IS NULL OR r.requestedBy.organizationCode = :orgCode) AND "
        + "(:status IS NULL OR r.status = :status)")
    Page<CriticalActionRequestEntity> findFiltered(
        @Param("requestedById") Long requestedById,
        @Param("orgCode") String orgCode,
        @Param("status") CriticalActionStatus status,
        Pageable pageable
    );
}
