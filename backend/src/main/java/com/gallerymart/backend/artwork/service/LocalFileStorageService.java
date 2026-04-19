package com.gallerymart.backend.artwork.service;

import com.gallerymart.backend.exception.InvalidInputException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String storeArtworkImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidInputException("Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new InvalidInputException("Only image files are allowed");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidInputException("Unsupported image format. Allowed: jpg, jpeg, png, webp, gif");
        }

        String fileName = UUID.randomUUID() + "." + extension;
        Path artworkUploadDir = Paths.get(uploadDir, "artworks").toAbsolutePath().normalize();
        Path targetFile = artworkUploadDir.resolve(fileName);

        try {
            Files.createDirectories(artworkUploadDir);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new InvalidInputException("Cannot store image file");
        }

        return "/uploads/artworks/" + fileName;
    }

    private String extractExtension(String fileName) {
        if (fileName == null) {
            throw new InvalidInputException("Invalid file name");
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new InvalidInputException("File extension is required");
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}