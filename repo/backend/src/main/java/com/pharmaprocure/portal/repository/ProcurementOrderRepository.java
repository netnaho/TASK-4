package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.ProcurementOrderEntity;
import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.enums.RoleName;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcurementOrderRepository extends JpaRepository<ProcurementOrderEntity, Long> {
    @EntityGraph(attributePaths = {"buyer", "items", "items.product"})
    Optional<ProcurementOrderEntity> findWithItemsById(Long id);

    @EntityGraph(attributePaths = {"buyer", "items", "items.product"})
    List<ProcurementOrderEntity> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"buyer", "items", "items.product"})
    List<ProcurementOrderEntity> findByBuyerRoleNameOrderByCreatedAtDesc(RoleName roleName);

    @EntityGraph(attributePaths = {"buyer", "items", "items.product"})
    List<ProcurementOrderEntity> findByBuyerOrganizationCodeOrderByCreatedAtDesc(String organizationCode);

    @EntityGraph(attributePaths = {"buyer", "items", "items.product"})
    List<ProcurementOrderEntity> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    @EntityGraph(attributePaths = {"buyer"})
    @Query("SELECT o FROM ProcurementOrderEntity o WHERE "
        + "(:buyerId IS NULL OR o.buyer.id = :buyerId) AND "
        + "(:orgCode IS NULL OR o.buyer.organizationCode = :orgCode) AND "
        + "(:roleName IS NULL OR o.buyer.role.name = :roleName) AND "
        + "(:status IS NULL OR o.currentStatus = :status)")
    Page<ProcurementOrderEntity> findFiltered(
        @Param("buyerId") Long buyerId,
        @Param("orgCode") String orgCode,
        @Param("roleName") RoleName roleName,
        @Param("status") OrderStatus status,
        Pageable pageable
    );
}
