package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.dto.AdminDtos.CreateReasonCodeRequest;
import com.pharmaprocure.portal.dto.AdminDtos.PermissionOverviewResponse;
import com.pharmaprocure.portal.dto.AdminDtos.ReasonCodeResponse;
import com.pharmaprocure.portal.dto.AdminDtos.StateMachineConfigResponse;
import com.pharmaprocure.portal.dto.AdminDtos.StateMachineTransitionResponse;
import com.pharmaprocure.portal.dto.AdminDtos.UpdateDocumentTypeRequest;
import com.pharmaprocure.portal.dto.AdminDtos.UpdateReasonCodeRequest;
import com.pharmaprocure.portal.dto.AdminDtos.UserVisibilityResponse;
import com.pharmaprocure.portal.entity.DocumentTypeEntity;
import com.pharmaprocure.portal.entity.ReasonCodeEntity;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.exception.ApiException;
import com.pharmaprocure.portal.repository.DocumentTypeRepository;
import com.pharmaprocure.portal.repository.OrderStateMachineDefinitionRepository;
import com.pharmaprocure.portal.repository.ReasonCodeRepository;
import com.pharmaprocure.portal.repository.UserRepository;
import com.pharmaprocure.portal.security.RolePermissionMatrix;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final RolePermissionMatrix rolePermissionMatrix;
    private final OrderStateMachineDefinitionRepository stateMachineDefinitionRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final ReasonCodeRepository reasonCodeRepository;

    public AdminService(
        UserRepository userRepository,
        RolePermissionMatrix rolePermissionMatrix,
        OrderStateMachineDefinitionRepository stateMachineDefinitionRepository,
        DocumentTypeRepository documentTypeRepository,
        ReasonCodeRepository reasonCodeRepository
    ) {
        this.userRepository = userRepository;
        this.rolePermissionMatrix = rolePermissionMatrix;
        this.stateMachineDefinitionRepository = stateMachineDefinitionRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.reasonCodeRepository = reasonCodeRepository;
    }

    @Transactional(readOnly = true)
    public List<UserVisibilityResponse> users() {
        return userRepository.findAll().stream()
            .map(user -> new UserVisibilityResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole().getName().name(), user.isActive()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionOverviewResponse> permissions() {
        return Arrays.stream(RoleName.values())
            .map(role -> new PermissionOverviewResponse(role.name(), rolePermissionMatrix.getPermissions(role).stream().map(Enum::name).sorted().toList()))
            .toList();
    }

    @Transactional(readOnly = true)
    public StateMachineConfigResponse stateMachine() {
        return new StateMachineConfigResponse(
            stateMachineDefinitionRepository.findByActiveTrue().stream()
                .map(item -> new StateMachineTransitionResponse(item.getFromStatus(), item.getToStatus(), item.isActive()))
                .toList()
        );
    }

    @Transactional(readOnly = true)
    public List<com.pharmaprocure.portal.dto.DocumentDtos.DocumentTypeResponse> documentTypes() {
        return documentTypeRepository.findAll().stream()
            .map(type -> new com.pharmaprocure.portal.dto.DocumentDtos.DocumentTypeResponse(type.getId(), type.getCode(), type.getName(), type.getDescription(), type.isEvidenceAllowed(), type.isActive()))
            .toList();
    }

    @Transactional
    public com.pharmaprocure.portal.dto.DocumentDtos.DocumentTypeResponse updateDocumentType(Long id, UpdateDocumentTypeRequest request) {
        DocumentTypeEntity type = documentTypeRepository.findById(id)
            .orElseThrow(() -> new ApiException(404, "Document type not found", List.of("documentTypeId=" + id)));
        type.setDescription(request.description());
        type.setEvidenceAllowed(request.evidenceAllowed());
        type.setActive(request.active());
        documentTypeRepository.save(type);
        return new com.pharmaprocure.portal.dto.DocumentDtos.DocumentTypeResponse(type.getId(), type.getCode(), type.getName(), type.getDescription(), type.isEvidenceAllowed(), type.isActive());
    }

    @Transactional(readOnly = true)
    public List<ReasonCodeResponse> reasonCodes() {
        return reasonCodeRepository.findAll().stream().map(this::toReasonCode).toList();
    }

    @Transactional
    public ReasonCodeResponse createReasonCode(CreateReasonCodeRequest request) {
        ReasonCodeEntity entity = new ReasonCodeEntity();
        entity.setCodeType(request.codeType());
        entity.setCode(request.code());
        entity.setLabel(request.label());
        entity.setActive(request.active());
        return toReasonCode(reasonCodeRepository.save(entity));
    }

    @Transactional
    public ReasonCodeResponse updateReasonCode(Long id, UpdateReasonCodeRequest request) {
        ReasonCodeEntity entity = reasonCodeRepository.findById(id)
            .orElseThrow(() -> new ApiException(404, "Reason code not found", List.of("reasonCodeId=" + id)));
        entity.setLabel(request.label());
        entity.setActive(request.active());
        return toReasonCode(reasonCodeRepository.save(entity));
    }

    private ReasonCodeResponse toReasonCode(ReasonCodeEntity entity) {
        return new ReasonCodeResponse(entity.getId(), entity.getCodeType(), entity.getCode(), entity.getLabel(), entity.isActive());
    }
}
