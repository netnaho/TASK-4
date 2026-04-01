package com.pharmaprocure.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record UserVisibilityResponse(Long id, String username, String displayName, String role, boolean active) {
    }

    public record PermissionOverviewResponse(String role, List<String> permissions) {
    }

    public record StateMachineTransitionResponse(String fromStatus, String toStatus, boolean active) {
    }

    public record ReasonCodeResponse(Long id, String codeType, String code, String label, boolean active) {
    }

    public record UpdateDocumentTypeRequest(@NotBlank String description, boolean evidenceAllowed, boolean active) {
    }

    public record UpdateReasonCodeRequest(@NotBlank String label, boolean active) {
    }

    public record CreateReasonCodeRequest(@NotBlank String codeType, @NotBlank String code, @NotBlank String label, boolean active) {
    }

    public record StateMachineConfigResponse(List<StateMachineTransitionResponse> transitions) {
    }
}
