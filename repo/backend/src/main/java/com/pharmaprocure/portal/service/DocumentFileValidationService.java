package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentFileValidationService {

    private static final long MAX_SIZE = 25L * 1024L * 1024L;

    private static final Map<String, List<byte[]>> SIGNATURES = Map.of(
        "application/pdf", List.of(new byte[] {0x25, 0x50, 0x44, 0x46}),
        "image/png", List.of(new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47}),
        "image/jpeg", List.of(new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff}),
        "audio/wav", List.of(new byte[] {0x52, 0x49, 0x46, 0x46})
    );

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(400, "Document file is required", List.of("EMPTY_FILE"));
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ApiException(400, "File exceeds maximum size", List.of("MAX_25_MB"));
        }
        String mimeType = file.getContentType();
        if (!SIGNATURES.containsKey(mimeType)) {
            throw new ApiException(400, "Unsupported file type", List.of("ALLOWED_TYPES: PDF/JPG/PNG/WAV"));
        }
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(12);
            boolean valid = SIGNATURES.get(mimeType).stream().anyMatch(signature -> startsWith(header, signature));
            if (!valid) {
                throw new ApiException(400, "File signature does not match MIME type", List.of("INVALID_SIGNATURE"));
            }
            if ("audio/wav".equals(mimeType) && header.length >= 12) {
                if (!(startsWith(header, new byte[] {0x52, 0x49, 0x46, 0x46}) && header[8] == 0x57 && header[9] == 0x41 && header[10] == 0x56 && header[11] == 0x45)) {
                    throw new ApiException(400, "File signature does not match MIME type", List.of("INVALID_SIGNATURE"));
                }
            }
        } catch (IOException ex) {
            throw new ApiException(400, "Unable to read uploaded file", List.of("READ_ERROR"));
        }
    }

    private boolean startsWith(byte[] actual, byte[] prefix) {
        if (actual.length < prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if (actual[index] != prefix[index]) {
                return false;
            }
        }
        return true;
    }
}
