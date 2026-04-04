package com.pharmaprocure.portal.integration;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.entity.CheckInEntity;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.repository.CheckInRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

class CheckInControllerIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Autowired
    private CheckInRepository checkInRepository;

    @Test
    void createPersistsReportedDeviceTimeAndOptionalCoordinates() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-checkin-hardening", "ORG-ALPHA", "Password!23");

        createCheckIn(buyer, "Initial check-in", "2000-01-01T00:00:00Z", "12.345678", "98.765432")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deviceTimestamp").value("2000-01-01T00:00:00Z"))
            .andExpect(jsonPath("$.latitude").value(12.345678))
            .andExpect(jsonPath("$.longitude").value(98.765432));
    }

    @Test
    void createUsesReportedFieldsAndUpdateCreatesHighlightedRevision() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-checkin-server-fields", "ORG-ALPHA", "Password!23");

        createCheckIn(buyer, "Initial check-in", "2026-03-29T10:00:00Z", null, null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentText").value("Initial check-in"))
            .andExpect(jsonPath("$.deviceTimestamp").value("2026-03-29T10:00:00Z"))
            .andExpect(jsonPath("$.latitude").isEmpty())
            .andExpect(jsonPath("$.longitude").isEmpty());

        CheckInEntity saved = checkInRepository.findAll().get(0);

        MockMultipartFile updatePayload = new MockMultipartFile(
            "payload",
            "payload.json",
            MULTIPART_FORM_DATA.toString(),
            json(java.util.Map.of(
                "commentText", "Updated note",
                "deviceTimestamp", "2040-01-01T00:00:00Z",
                "latitude", "0.000001",
                "longitude", "0.000002"
            )).getBytes()
        );
        MockMultipartFile evidence = new MockMultipartFile("files", "evidence.png", "image/png", java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jx7sAAAAASUVORK5CYII="));

        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT, "/api/check-ins/{checkInId}", saved.getId())
                .file(updatePayload)
                .file(evidence)
                .with(authenticated(buyer))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentText").value("Updated note"))
            .andExpect(jsonPath("$.deviceTimestamp").value("2040-01-01T00:00:00Z"))
            .andExpect(jsonPath("$.latitude").value(0.000001))
            .andExpect(jsonPath("$.longitude").value(0.000002))
            .andExpect(jsonPath("$.revisions[0].changedFields").isArray())
            .andExpect(jsonPath("$.revisions[0].attachments[0].originalFileName").value("evidence.png"));
    }

    @Test
    void checkInReadRespectsScopeBoundaries() throws Exception {
        var owner = createUser(RoleName.BUYER, "buyer-checkin-owner", "ORG-ALPHA", "Password!23");
        var otherBuyer = createUser(RoleName.BUYER, "buyer-checkin-other", "ORG-ALPHA", "Password!23");

        createCheckIn(owner, "Scoped record", null, null, null)
            .andExpect(status().isOk());

        Long checkInId = checkInRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/check-ins/{checkInId}", checkInId).with(authenticated(otherBuyer)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.details[0]").value("CHECKIN_SCOPE_RESTRICTION"));
    }

    @Test
    void checkInMultipartPayloadAppliesBeanValidation() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-checkin-validation", "ORG-ALPHA", "Password!23");

        createCheckIn(buyer, "Invalid coordinates", "2026-03-29T10:00:00Z", "120.000000", "38.761200")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    private ResultActions createCheckIn(com.pharmaprocure.portal.entity.UserEntity user, String commentText, String deviceTimestamp, String latitude, String longitude) throws Exception {
        java.util.Map<String, Object> payloadBody = new java.util.LinkedHashMap<>();
        payloadBody.put("commentText", commentText);
        if (deviceTimestamp != null) {
            payloadBody.put("deviceTimestamp", deviceTimestamp);
        }
        if (latitude != null) {
            payloadBody.put("latitude", latitude);
        }
        if (longitude != null) {
            payloadBody.put("longitude", longitude);
        }
        MockMultipartFile payload = new MockMultipartFile(
            "payload",
            "payload.json",
            MULTIPART_FORM_DATA.toString(),
            json(payloadBody).getBytes()
        );

        return mockMvc.perform(multipart("/api/check-ins")
            .file(payload)
            .with(authenticated(user))
            .with(csrf()));
    }
}
