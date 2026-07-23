package io.github.mevanlc.jnicetouch.swingdemo;

import static org.testng.Assert.assertEquals;

import io.github.mevanlc.jnicetouch.EventKind;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import org.testng.annotations.Test;

public class GestureCanvasRenderTest {
    @Test
    public void rendersTheInteractiveStateOffscreen() throws Exception {
        BufferedImage image = new BufferedImage(720, 430, BufferedImage.TYPE_INT_ARGB);

        SwingUtilities.invokeAndWait(() -> {
            GestureState state = new GestureState();
            state.apply(TestEvents.event(EventKind.SMART_MAGNIFY));
            GestureCanvas canvas = new GestureCanvas(state);
            BufferedImage surface = new BufferedImage(720, 430, BufferedImage.TYPE_INT_RGB);
            Graphics2D surfaceGraphics = surface.createGraphics();
            try {
                surfaceGraphics.setColor(new java.awt.Color(0xD88A52));
                surfaceGraphics.fillRect(0, 0, surface.getWidth(), surface.getHeight());
            } finally {
                surfaceGraphics.dispose();
            }
            canvas.setSurfaceImage(surface, "render-test.jpg");
            assertEquals(state.scale(), 1.0);
            canvas.setSize(720, 430);
            Graphics2D graphics = image.createGraphics();
            try {
                canvas.paint(graphics);
            } finally {
                graphics.dispose();
            }
        });

        assertEquals(image.getRGB(360, 215), new java.awt.Color(0xD88A52).getRGB());
        assertEquals(image.getRGB(5, 5), new java.awt.Color(0xD88A52).getRGB());
    }
}
