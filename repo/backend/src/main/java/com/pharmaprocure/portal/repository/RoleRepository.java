package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.RoleEntity;
import com.pharmaprocure.portal.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleName name);
}
