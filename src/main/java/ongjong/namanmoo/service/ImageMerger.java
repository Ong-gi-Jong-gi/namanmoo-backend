package ongjong.namanmoo.service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ImageMerger {

    public static BufferedImage mergeImages(List<URL> imageUrls) throws IOException {

        // 이 메소드가 호출되기 전에 imageFiles 목록이 null 이나 비어 있지 않은지 확인하세요.

        if (imageUrls.size() != 4) {
            throw new IllegalArgumentException("정확히 4개의 이미지가 필요합니다");
        }

        // 이미지 불러오기
        BufferedImage img1 = null;
        BufferedImage img2 = null;
        BufferedImage img3 = null;
        BufferedImage img4 = null;

        // 이미지 파일이 올바른 경로를 가지고 읽을 수 있는지 확인하세요.
        try {
            img1 = ImageIO.read(imageUrls.get(0));
            img2 = ImageIO.read(imageUrls.get(1));
            img3 = ImageIO.read(imageUrls.get(2));
            img4 = ImageIO.read(imageUrls.get(3));
        }
        catch(IOException e) {
            for(URL url: imageUrls) {
                System.out.println("URL 경로: " + url.toString());
            }
            throw e; // 로깅 후 예외를 다시 던집니다.
        }

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