package io.github.mevanlc.jnicetouch.swingdemo;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.testng.annotations.Test;

public class SurfaceImageLoaderTest {
    @Test
    public void preservesPhotoResolutionWithinTheWorkspaceLimit() throws Exception {
        Path path = Files.createTempFile("jnicetouch-surface-", ".jpg");
        try {
            BufferedImage source = new BufferedImage(2200, 1100, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = source.createGraphics();
            try {
                graphics.setColor(new Color(0x31566B));
                graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
            } finally {
                graphics.dispose();
            }
            assertTrue(ImageIO.write(source, "jpg", path.toFile()));

            BufferedImage loaded = SurfaceImageLoader.load(path);

            assertEquals(loaded.getWidth(), 2200);
            assertEquals(loaded.getHeight(), 1100);
            assertTrue(Math.max(loaded.getWidth(), loaded.getHeight())
                    <= SurfaceImageLoader.MAX_DIMENSION);
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    public void subsamplesOnlyImagesLargerThanTheWorkspaceLimit() {
        assertEquals(SurfaceImageLoader.subsamplingFor(7952, 5304), 1);
        assertEquals(SurfaceImageLoader.subsamplingFor(16_384, 8192), 2);
    }
}
