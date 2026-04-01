package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DocumentSignatureServiceTest {

    @Test
    void generatesSignatureMetadata() throws Exception {
        Path directory = Files.createTempDirectory("signature-test");
        Path keyPath = directory.resolve("doc.key");
        DocumentSignatureService service = new DocumentSignatureService(keyPath.toString());
        String signature = service.sign("abcdef");
        assertFalse(signature.isBlank());
        assertEquals("HmacSHA256", service.algorithm());
        assertEquals("doc.key", service.keyId());
        Files.deleteIfExists(keyPath);
        Files.deleteIfExists(directory);
    }
}
