package com.pharmaprocure.portal.integration;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.entity.DocumentEntity;
import com.pharmaprocure.portal.entity.DocumentTypeEntity;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.repository.DocumentRepository;
import com.pharmaprocure.portal.repository.DocumentTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

class DocumentCenterControllerIntegrationTest extends AbstractMockMvcIntegrationTest {

    private static final byte[] PNG_BYTES = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jx7sAAAAASUVORK5CYII=");

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Test
    void previewContentReturnsWatermarkedBytes() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-document-preview", "ORG-ALPHA", "Password!23");
        DocumentTypeEntity type = ensureDocumentType();

        createDocument(buyer, type)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Controlled PNG"));

        DocumentEntity document = documentRepository.findAll().get(0);

        mockMvc.perform(get("/api/documents/{documentId}/preview", document.getId()).with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.previewSupported").value(true));

        byte[] response = mockMvc.perform(get("/api/documents/{documentId}/content", document.getId()).with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/png"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        org.junit.jupiter.api.Assertions.assertFalse(java.util.Arrays.equals(PNG_BYTES, response), "Preview content should include a server-generated watermark");
    }

    @Test
    void documentReadRespectsScopeBoundaries() throws Exception {
        var owner = createUser(RoleName.BUYER, "buyer-document-owner", "ORG-ALPHA", "Password!23");
        var otherBuyer = createUser(RoleName.BUYER, "buyer-document-other", "ORG-ALPHA", "Password!23");
        DocumentTypeEntity type = ensureDocumentType();

        createDocument(owner, type).andExpect(status().isOk());
        Long documentId = documentRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/documents/{documentId}", documentId).with(authenticated(otherBuyer)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.details[0]").value("DOCUMENT_SCOPE_RESTRICTION"));
    }

    @Test
    void documentMultipartPayloadAppliesBeanValidation() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-document-validation", "ORG-ALPHA", "Password!23");
        DocumentTypeEntity type = ensureDocumentType();

        MockMultipartFile payload = new MockMultipartFile(
            "payload",
            "payload.json",
            MULTIPART_FORM_DATA.toString(),
            json(java.util.Map.of(
                "documentTypeId", type.getId(),
                "title", "",
                "contentText", "Preview body",
                "approvalRoles", java.util.List.of()
            )).getBytes()
        );

        mockMvc.perform(multipart("/api/documents")
                .file(payload)
                .with(authenticated(buyer))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    private ResultActions createDocument(com.pharmaprocure.portal.entity.UserEntity user, DocumentTypeEntity type) throws Exception {
        MockMultipartFile payload = new MockMultipartFile(
            "payload",
            "payload.json",
            MULTIPART_FORM_DATA.toString(),
            json(java.util.Map.of(
                "documentTypeId", type.getId(),
                "title", "Controlled PNG",
                "contentText", "Preview body",
                "metadataTags", "preview",
                "approvalRoles", java.util.List.of("QUALITY_REVIEWER")
            )).getBytes()
        );
        MockMultipartFile file = new MockMultipartFile("file", "controlled.png", "image/png", PNG_BYTES);
        return mockMvc.perform(multipart("/api/documents")
            .file(payload)
            .file(file)
            .with(authenticated(user))
            .with(csrf()));
    }

    private DocumentTypeEntity ensureDocumentType() {
        return documentTypeRepository.findByCode("TEST-DOC")
            .orElseGet(() -> {
                DocumentTypeEntity type = new DocumentTypeEntity();
                type.setCode("TEST-DOC");
                type.setName("Test Document");
                type.setDescription("Integration test document type");
                type.setEvidenceAllowed(false);
                type.setActive(true);
                return documentTypeRepository.save(type);
            });
    }
}
