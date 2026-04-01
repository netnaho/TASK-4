package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.pharmaprocure.portal.entity.OrderStateMachineDefinitionEntity;
import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.exception.ApiException;
import com.pharmaprocure.portal.repository.OrderStateMachineDefinitionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OrderStateMachineServiceTest {

    private final OrderStateMachineDefinitionRepository repository = Mockito.mock(OrderStateMachineDefinitionRepository.class);
    private final OrderStateMachineService service = new OrderStateMachineService(repository);

    @Test
    void acceptsConfiguredTransition() {
        OrderStateMachineDefinitionEntity definition = new OrderStateMachineDefinitionEntity();
        definition.setFromStatus("CREATED");
        definition.setToStatus("UNDER_REVIEW");
        definition.setActive(true);
        when(repository.findByActiveTrue()).thenReturn(List.of(definition));
        assertDoesNotThrow(() -> service.validateTransition(OrderStatus.CREATED, OrderStatus.UNDER_REVIEW));
    }

    @Test
    void rejectsInvalidTransition() {
        when(repository.findByActiveTrue()).thenReturn(List.of());
        assertThrows(ApiException.class, () -> service.validateTransition(OrderStatus.CREATED, OrderStatus.SHIPPED));
    }
}
