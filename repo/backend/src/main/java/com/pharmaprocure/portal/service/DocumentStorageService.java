package com.pharmaprocure.portal.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentStorageService {

    private final Path rootPath;

    public DocumentStorageService(@Value("${application.document.storage-root:/app/data/documents}") String storageRoot) {
        this.rootPath = Path.of(storageRoot);
    }

    public StoredFileMetadata store(String typeCode, String documentNumber, int versionNumber, MultipartFile file) {
        try {
            String extension = extension(file.getOriginalFilename());
            String storedFileName = UUID.randomUUID() + extension;
            Path relativePath = Path.of(typeCode, Integer.toString(Year.now().getValue()), documentNumber, "v" + versionNumber, storedFileName);
            Path targetPath = rootPath.resolve(relativePath).normalize();
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFileMetadata(file.getOriginalFilename(), storedFileName, relativePath.toString(), file.getContentType(), file.getSize(), targetPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store document file", ex);
        }
    }

    public Path resolve(String relativePath) {
        return rootPath.resolve(relativePath).normalize();
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    public record StoredFileMetadata(
        String originalFileName,
        String storedFileName,
        String storagePath,
        String mimeType,
        long fileSizeBytes,
        Path absolutePath
    ) {
    }
}
