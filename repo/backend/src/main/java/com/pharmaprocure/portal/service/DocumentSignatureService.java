package com.pharmaprocure.portal.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DocumentSignatureService {

    private final Path keyPath;

    public DocumentSignatureService(@Value("${application.document.signature-key-path:/app/data/keys/document-signing.key}") String keyPath) {
        this.keyPath = Path.of(keyPath);
    }

    public String sign(String hashValue) {
        try {
            byte[] key = ensureKey();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(hashValue.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign archive hash", ex);
        }
    }

    public String keyId() {
        return keyPath.getFileName().toString();
    }

    public String algorithm() {
        return "HmacSHA256";
    }

    private byte[] ensureKey() throws IOException {
        Files.createDirectories(keyPath.getParent());
        if (!Files.exists(keyPath)) {
            byte[] key = new byte[32];
            new SecureRandom().nextBytes(key);
            Files.writeString(keyPath, Base64.getEncoder().encodeToString(key), StandardCharsets.UTF_8);
            return key;
        }
        return Base64.getDecoder().decode(Files.readString(keyPath, StandardCharsets.UTF_8));
    }
}
