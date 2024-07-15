package ongjong.namanmoo.service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageMerger {

    public static BufferedImage mergeImages(List<File> imageFiles) throws IOException {
        if (imageFiles.size() != 4) {
            throw new IllegalArgumentException("Exactly 4 images are required");
        }

        // Load images
        BufferedImage img1 = ImageIO.read(imageFiles.get(0));
        BufferedImage img2 = ImageIO.read(imageFiles.get(1));
        BufferedImage img3 = ImageIO.read(imageFiles.get(2));
        BufferedImage img4 = ImageIO.read(imageFiles.get(3));

        // Assume all images are the same size
        int width = img1.getWidth();
        int height = img1.getHeight();

        // Create a new image with double width and height
        BufferedImage combinedImage = new BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_ARGB);

        // Draw images into the combined image
        Graphics2D g = combinedImage.createGraphics();
        g.drawImage(img1, 0, 0, null);
        g.drawImage(img2, width, 0, null);
        g.drawImage(img3, 0, height, null);
        g.drawImage(img4, width, height, null);
        g.dispose();

        return combinedImage;
    }
}