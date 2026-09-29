package com.ecommerce.catalog.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ProductImageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public record ImagePaths(String imageUrl, String thumbnailUrl) {}

    public ImagePaths store(Long productId, MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("Uploaded file is empty");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String baseName = productId + "-" + UUID.randomUUID();
            File fullFile = new File(dir, baseName + "-full.jpg");
            File thumbFile = new File(dir, baseName + "-thumb.jpg");

            Thumbnails.of(file.getInputStream()).size(1200, 1200).outputFormat("jpg").toFile(fullFile);
            Thumbnails.of(file.getInputStream()).size(300, 300).outputFormat("jpg").toFile(thumbFile);

            return new ImagePaths("/images/products/" + fullFile.getName(), "/images/products/" + thumbFile.getName());
        } catch (IOException e) {
            throw new IllegalStateException("Could not process image: " + e.getMessage());
        }
    }

    public void deleteFiles(String... urls) {
        for (String url : urls) {
            if (url == null) continue;
            File f = new File(uploadDir, url.substring(url.lastIndexOf('/') + 1));
            if (f.exists()) f.delete();
        }
    }
}