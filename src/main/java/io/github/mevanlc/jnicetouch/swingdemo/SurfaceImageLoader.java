package io.github.mevanlc.jnicetouch.swingdemo;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

final class SurfaceImageLoader {
    static final int MAX_DIMENSION = 8192;

    private SurfaceImageLoader() {}

    static BufferedImage load(Path path) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(path.toFile())) {
            if (input == null) {
                throw new IOException("Could not open " + path);
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("No ImageIO reader supports " + path.getFileName());
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                ImageReadParam parameters = reader.getDefaultReadParam();
                int subsampling = subsamplingFor(width, height);
                parameters.setSourceSubsampling(subsampling, subsampling, 0, 0);
                BufferedImage image = reader.read(0, parameters);
                if (image == null) {
                    throw new IOException("Could not decode " + path.getFileName());
                }
                return image;
            } finally {
                reader.dispose();
            }
        }
    }

    static int subsamplingFor(int width, int height) {
        int largestDimension = Math.max(width, height);
        return Math.max(1, (largestDimension + MAX_DIMENSION - 1) / MAX_DIMENSION);
    }
}
