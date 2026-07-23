package io.github.mevanlc.jnicetouch.swingdemo;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.awt.image.BufferedImage;
import org.testng.annotations.Test;

public class AppIconTest {
    @Test
    public void loadsTheGeneratedHighResolutionSwingIcon() {
        BufferedImage image = AppIcon.image();

        assertEquals(image.getWidth(), 1024);
        assertEquals(image.getHeight(), 1024);
        assertEquals(image.getRGB(0, 0), 0);
        assertNotEquals(image.getRGB(512, 512), 0);
    }
}
