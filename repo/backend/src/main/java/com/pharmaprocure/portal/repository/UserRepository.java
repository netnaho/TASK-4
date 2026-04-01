package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    @Query("select u from UserEntity u join fetch u.role where u.username = :username")
    Optional<UserEntity> findWithRoleByUsername(@Param("username") String username);
}
