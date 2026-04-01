package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DocumentHashingServiceTest {

    private final DocumentHashingService service = new DocumentHashingService();

    @Test
    void generatesExpectedSha256Hash() throws Exception {
        Path path = Files.createTempFile("hash-test", ".txt");
        Files.writeString(path, "document-center-hash");
        assertEquals("e807032dea65778aa95595b5bdcb4612202b0bfcbf8082e3df5a72911c398a6c", service.sha256(path));
        Files.deleteIfExists(path);
    }
}
