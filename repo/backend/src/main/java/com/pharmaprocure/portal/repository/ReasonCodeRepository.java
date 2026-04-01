package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.ReasonCodeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReasonCodeRepository extends JpaRepository<ReasonCodeEntity, Long> {
    List<ReasonCodeEntity> findByCodeTypeOrderByLabelAsc(String codeType);
}
