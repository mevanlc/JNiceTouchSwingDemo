package io.github.mevanlc.jnicetouch.swingdemo;

import java.awt.Taskbar;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

final class AppIcon {
    private static final String RESOURCE = "/appicon.png";
    private static final BufferedImage IMAGE = loadImage();

    private AppIcon() {}

    static void install(Window window) {
        window.setIconImage(IMAGE);
        if (Taskbar.isTaskbarSupported()
                && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
            try {
                Taskbar.getTaskbar().setIconImage(IMAGE);
            } catch (SecurityException | UnsupportedOperationException ignored) {
                // The packaged macOS app already receives the same art through jpackage.
            }
        }
    }

    static BufferedImage image() {
        return IMAGE;
    }

    private static BufferedImage loadImage() {
        try (InputStream input = AppIcon.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing Swing app icon: " + RESOURCE);
            }
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new IllegalStateException("Unsupported Swing app icon: " + RESOURCE);
            }
            return image;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
