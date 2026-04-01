package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.CheckInEntity;
import com.pharmaprocure.portal.enums.RoleName;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CheckInRepository extends JpaRepository<CheckInEntity, Long> {
    @EntityGraph(attributePaths = {"ownerUser"})
    List<CheckInEntity> findAllByOrderByUpdatedAtDesc();

    @EntityGraph(attributePaths = {"ownerUser"})
    List<CheckInEntity> findByOwnerUserIdOrderByUpdatedAtDesc(Long ownerUserId);

    @EntityGraph(attributePaths = {"ownerUser"})
    List<CheckInEntity> findByOwnerUserRoleNameOrderByUpdatedAtDesc(RoleName roleName);

    @EntityGraph(attributePaths = {"ownerUser"})
    List<CheckInEntity> findByOwnerUserOrganizationCodeOrderByUpdatedAtDesc(String organizationCode);

    @EntityGraph(attributePaths = {"ownerUser"})
    Optional<CheckInEntity> findWithOwnerUserById(Long id);

    @EntityGraph(attributePaths = {"ownerUser"})
    @Query("SELECT c FROM CheckInEntity c WHERE "
        + "(:ownerUserId IS NULL OR c.ownerUser.id = :ownerUserId) AND "
        + "(:orgCode IS NULL OR c.ownerUser.organizationCode = :orgCode) AND "
        + "(:roleName IS NULL OR c.ownerUser.role.name = :roleName)")
    Page<CheckInEntity> findFiltered(
        @Param("ownerUserId") Long ownerUserId,
        @Param("orgCode") String orgCode,
        @Param("roleName") RoleName roleName,
        Pageable pageable
    );
}
