package io.github.mevanlc.jnicetouch.swingdemo;

import static org.testng.Assert.assertNotEquals;

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
            canvas.setSize(720, 430);
            Graphics2D graphics = image.createGraphics();
            try {
                canvas.paint(graphics);
            } finally {
                graphics.dispose();
            }
        });

        assertNotEquals(image.getRGB(360, 215), image.getRGB(5, 5));
    }
}
