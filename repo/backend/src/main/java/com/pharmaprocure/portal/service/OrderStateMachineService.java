package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.exception.ApiException;
import com.pharmaprocure.portal.repository.OrderStateMachineDefinitionRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class OrderStateMachineService {

    private final OrderStateMachineDefinitionRepository repository;

    public OrderStateMachineService(OrderStateMachineDefinitionRepository repository) {
        this.repository = repository;
    }

    public void validateTransition(OrderStatus from, OrderStatus to) {
        Set<String> transitions = repository.findByActiveTrue().stream()
            .map(definition -> definition.getFromStatus() + "->" + definition.getToStatus())
            .collect(Collectors.toSet());
        String key = from.name() + "->" + to.name();
        if (!transitions.contains(key)) {
            throw new ApiException(400, "Invalid order status transition", java.util.List.of(key));
        }
    }
}
