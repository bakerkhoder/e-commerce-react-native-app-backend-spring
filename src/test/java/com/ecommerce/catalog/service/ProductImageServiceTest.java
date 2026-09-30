package com.ecommerce.catalog.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.*;

class ProductImageServiceTest {

    private final ProductImageService service = new ProductImageService();

    {
        ReflectionTestUtils.setField(service, "uploadDir", "target/test-uploads");
    }

    private byte[] realJpegBytes(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        return out.toByteArray();
    }

    @Test
    void store_rejectsFile_whenContentTypeClaimsImageButBytesAreNotActuallyAnImage() {
        MockMultipartFile fakeImage = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", "not actually image data".getBytes());

        assertThatThrownBy(() -> service.store(1L, fakeImage))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not a valid image");
    }

    @Test
    void store_rejectsFile_whenEmpty() {
        MockMultipartFile empty = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> service.store(1L, empty))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("empty");
    }

    @Test
    void store_acceptsAndProcesses_aGenuineSmallImage() throws Exception {
        MockMultipartFile realImage = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", realJpegBytes(100, 100));

        ProductImageService.ImagePaths result = service.store(1L, realImage);

        assertThat(result.imageUrl()).endsWith("-full.jpg");
        assertThat(result.thumbnailUrl()).endsWith("-thumb.jpg");
    }
}