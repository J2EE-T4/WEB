package com.interacthub.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.interacthub.dto.media.MediaUploadResultDto;
import com.interacthub.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service @RequiredArgsConstructor @Slf4j
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public MediaUploadResultDto uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("File is empty");
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
            String url = (String) result.get("secure_url");
            String mediaType = file.getContentType() != null && file.getContentType().startsWith("image") ? "IMAGE" : "VIDEO";
            return new MediaUploadResultDto(url, mediaType);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    public void deleteFile(String url) {
        if (url == null || url.isBlank()) return;
        try {
            String publicId = extractPublicId(url);
            if (publicId != null) cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("Failed to delete file from Cloudinary: {}", e.getMessage());
        }
    }

    private String extractPublicId(String url) {
        try {
            String[] parts = url.split("/upload/");
            if (parts.length > 1) {
                String path = parts[1];
                if (path.contains("/")) path = path.substring(path.indexOf('/') + 1);
                return path.substring(0, path.lastIndexOf('.'));
            }
        } catch (Exception ignored) {}
        return null;
    }
}
