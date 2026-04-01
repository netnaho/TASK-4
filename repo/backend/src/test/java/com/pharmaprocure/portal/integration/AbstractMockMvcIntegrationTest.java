package com.pharmaprocure.portal.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pharmaprocure.portal.entity.OrderStateMachineDefinitionEntity;
import com.pharmaprocure.portal.entity.ProcurementOrderEntity;
import com.pharmaprocure.portal.entity.ProcurementOrderItemEntity;
import com.pharmaprocure.portal.entity.ProductCatalogEntity;
import com.pharmaprocure.portal.entity.RoleEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.repository.OrderStateMachineDefinitionRepository;
import com.pharmaprocure.portal.repository.ProcurementOrderRepository;
import com.pharmaprocure.portal.repository.ProductCatalogRepository;
import com.pharmaprocure.portal.repository.RoleRepository;
import com.pharmaprocure.portal.repository.UserRepository;
import com.pharmaprocure.portal.security.RolePermissionMatrix;
import com.pharmaprocure.portal.security.UserPrincipal;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class AbstractMockMvcIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProductCatalogRepository productCatalogRepository;

    @Autowired
    protected ProcurementOrderRepository orderRepository;

    @Autowired
    protected OrderStateMachineDefinitionRepository orderStateMachineDefinitionRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected RolePermissionMatrix rolePermissionMatrix;

    @BeforeEach
    void seedStateMachine() {
        if (!orderStateMachineDefinitionRepository.findByActiveTrue().isEmpty()) {
            return;
        }
        saveTransition(OrderStatus.CREATED, OrderStatus.UNDER_REVIEW);
        saveTransition(OrderStatus.UNDER_REVIEW, OrderStatus.APPROVED);
        saveTransition(OrderStatus.UNDER_REVIEW, OrderStatus.CANCELED);
        saveTransition(OrderStatus.APPROVED, OrderStatus.PAYMENT_RECORDED);
        saveTransition(OrderStatus.PAYMENT_RECORDED, OrderStatus.PICK_PACK);
        saveTransition(OrderStatus.PICK_PACK, OrderStatus.PARTIALLY_SHIPPED);
        saveTransition(OrderStatus.PICK_PACK, OrderStatus.SHIPPED);
        saveTransition(OrderStatus.PARTIALLY_SHIPPED, OrderStatus.SHIPPED);
        saveTransition(OrderStatus.PARTIALLY_SHIPPED, OrderStatus.RETURNED);
        saveTransition(OrderStatus.SHIPPED, OrderStatus.RECEIVED);
        saveTransition(OrderStatus.SHIPPED, OrderStatus.RETURNED);
        saveTransition(OrderStatus.RECEIVED, OrderStatus.RETURNED);
    }

    protected UserEntity createUser(RoleName roleName, String username, String organizationCode, String rawPassword) {
        RoleEntity role = roleRepository.findByName(roleName).orElseGet(() -> {
            RoleEntity entity = new RoleEntity();
            entity.setName(roleName);
            return roleRepository.save(entity);
        });
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setDisplayName(username);
        user.setOrganizationCode(organizationCode);
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    protected ProcurementOrderEntity createOrder(UserEntity buyer, OrderStatus status, int orderedQuantity, int shippedQuantity, int receivedQuantity) {
        ProductCatalogEntity product = new ProductCatalogEntity();
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        product.setSku("PP-TEST-" + suffix);
        product.setName("Test Product " + suffix);
        product.setUnit("box");
        product.setUnitPrice(BigDecimal.valueOf(12.50));
        product = productCatalogRepository.save(product);

        ProcurementOrderEntity order = new ProcurementOrderEntity();
        order.setOrderNumber("ORD-" + suffix);
        order.setBuyer(buyer);
        order.setCurrentStatus(status);
        order.setPaymentRecorded(status != OrderStatus.CREATED && status != OrderStatus.UNDER_REVIEW && status != OrderStatus.APPROVED);
        order.setReviewRequired(true);
        order.setCreatedAt(OffsetDateTime.now());
        order.setUpdatedAt(OffsetDateTime.now());

        ProcurementOrderItemEntity item = new ProcurementOrderItemEntity();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductNameSnapshot(product.getName());
        item.setSkuSnapshot(product.getSku());
        item.setUnitSnapshot(product.getUnit());
        item.setUnitPriceSnapshot(product.getUnitPrice());
        item.setOrderedQuantity(orderedQuantity);
        item.setShippedQuantity(shippedQuantity);
        item.setReceivedQuantity(receivedQuantity);
        item.setReturnedQuantity(0);
        item.setDamagedQuantity(0);
        item.setDiscrepancyFlag(false);
        order.getItems().add(item);

        return orderRepository.save(order);
    }

    protected RequestPostProcessor authenticated(UserEntity user) {
        return SecurityMockMvcRequestPostProcessors.user(principal(user));
    }

    protected String json(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    protected UserPrincipal principal(UserEntity user) {
        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getOrganizationCode(),
            user.getPasswordHash(),
            user.getRole().getName(),
            Set.copyOf(rolePermissionMatrix.getGrants(user.getRole().getName())),
            user.isActive()
        );
    }

    private void saveTransition(OrderStatus from, OrderStatus to) {
        OrderStateMachineDefinitionEntity entity = new OrderStateMachineDefinitionEntity();
        entity.setFromStatus(from.name());
        entity.setToStatus(to.name());
        entity.setActive(true);
        orderStateMachineDefinitionRepository.save(entity);
    }
}
