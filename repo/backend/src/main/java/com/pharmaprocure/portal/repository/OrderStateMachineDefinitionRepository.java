package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.OrderStateMachineDefinitionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStateMachineDefinitionRepository extends JpaRepository<OrderStateMachineDefinitionEntity, Long> {
    List<OrderStateMachineDefinitionEntity> findByActiveTrue();
}
