package com.ecommerce.catalog.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ProductImageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    // Generous enough for any real photo, small enough to block a deliberately oversized decode bomb
    private static final int MAX_DIMENSION = 6000;

    public record ImagePaths(String imageUrl, String thumbnailUrl) {}

    public ImagePaths store(Long productId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        BufferedImage image = decodeAndValidate(file);

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String baseName = productId + "-" + UUID.randomUUID();
            File fullFile = new File(dir, baseName + "-full.jpg");
            File thumbFile = new File(dir, baseName + "-thumb.jpg");

            // Resize from the already-decoded, already-validated image — never re-read the raw upload,
            // and never hand the resizer anything we haven't confirmed is a real, sane-sized image
            Thumbnails.of(image).size(1200, 1200).outputFormat("jpg").toFile(fullFile);
            Thumbnails.of(image).size(300, 300).outputFormat("jpg").toFile(thumbFile);

            return new ImagePaths("/images/products/" + fullFile.getName(), "/images/products/" + thumbFile.getName());
        } catch (IOException e) {
            throw new IllegalStateException("Could not process image: " + e.getMessage());
        }
    }

    /** Decodes the upload as a real image and rejects it outright if it isn't one, or is suspiciously large. */
    private BufferedImage decodeAndValidate(MultipartFile file) {
        BufferedImage image;
        try {
            image = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read uploaded file");
        }

        // A file with a spoofed image/* content-type that ISN'T actually a decodable image
        // (e.g. a script, an HTML payload, a corrupted file) returns null here rather than throwing
        if (image == null) {
            throw new IllegalArgumentException("File is not a valid image");
        }

        if (image.getWidth() > MAX_DIMENSION || image.getHeight() > MAX_DIMENSION) {
            throw new IllegalArgumentException(
                "Image dimensions too large (max " + MAX_DIMENSION + "x" + MAX_DIMENSION + ")");
        }

        return image;
    }

    public void deleteFiles(String... urls) {
        for (String url : urls) {
            if (url == null) continue;
            File f = new File(uploadDir, url.substring(url.lastIndexOf('/') + 1));
            if (f.exists()) f.delete();
        }
    }
}