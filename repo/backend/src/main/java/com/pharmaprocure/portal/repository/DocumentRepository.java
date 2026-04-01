package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.DocumentEntity;
import com.pharmaprocure.portal.enums.DocumentStatus;
import com.pharmaprocure.portal.enums.RoleName;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    @EntityGraph(attributePaths = {"documentType", "ownerUser", "template", "currentVersion"})
    List<DocumentEntity> findAllByOrderByUpdatedAtDesc();

    @EntityGraph(attributePaths = {"documentType", "ownerUser", "template", "currentVersion"})
    List<DocumentEntity> findByOwnerUserIdOrderByUpdatedAtDesc(Long ownerUserId);

    @EntityGraph(attributePaths = {"documentType", "ownerUser", "template", "currentVersion"})
    List<DocumentEntity> findByOwnerUserRoleNameOrderByUpdatedAtDesc(RoleName roleName);

    @EntityGraph(attributePaths = {"documentType", "ownerUser", "template", "currentVersion"})
    List<DocumentEntity> findByOwnerUserOrganizationCodeOrderByUpdatedAtDesc(String organizationCode);

    @EntityGraph(attributePaths = {"documentType", "ownerUser", "template", "currentVersion"})
    Optional<DocumentEntity> findWithCurrentVersionById(Long id);

    @EntityGraph(attributePaths = {"documentType", "ownerUser"})
    @Query("SELECT d FROM DocumentEntity d WHERE "
        + "(:ownerUserId IS NULL OR d.ownerUser.id = :ownerUserId) AND "
        + "(:orgCode IS NULL OR d.ownerUser.organizationCode = :orgCode) AND "
        + "(:roleName IS NULL OR d.ownerUser.role.name = :roleName) AND "
        + "(:status IS NULL OR d.status = :status) AND "
        + "(:documentTypeId IS NULL OR d.documentType.id = :documentTypeId)")
    Page<DocumentEntity> findFiltered(
        @Param("ownerUserId") Long ownerUserId,
        @Param("orgCode") String orgCode,
        @Param("roleName") RoleName roleName,
        @Param("status") DocumentStatus status,
        @Param("documentTypeId") Long documentTypeId,
        Pageable pageable
    );
}
