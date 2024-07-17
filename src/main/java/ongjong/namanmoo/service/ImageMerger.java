package ongjong.namanmoo.service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;


public class ImageMerger {

    public static BufferedImage mergeImages(List<BufferedImage> images) throws IOException {
        int size = images.size();
        if (size < 1 || size > 4) {
            throw new IllegalArgumentException("이미지는 최소 1개에서 최대 4개까지 병합할 수 있습니다.");
        }

        int width = 0;
        int height = 0;
        int padding = 10; // 여유 공간 패딩 값 설정

        // 최대 너비와 높이 계산
        for (BufferedImage image : images) {
            width = Math.max(width, image.getWidth());
            height = Math.max(height, image.getHeight());
        }

        // 병합될 최종 이미지 크기 계산 (여유 공간 추가)
        int finalWidth = size == 1 ? width : (width * 2) + padding;
        int finalHeight = size <= 2 ? height : (height * 2) + padding;

        BufferedImage mergedImage = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = mergedImage.createGraphics();

//        // 배경을 흰색으로 설정 (투명 배경을 원하면 이 부분을 제거)
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, finalWidth, finalHeight);

        // 이미지 병합 및 중앙 정렬 (여유 공간 포함)
        for (int i = 0; i < size; i++) {
            int imgWidth = images.get(i).getWidth();
            int imgHeight = images.get(i).getHeight();
            int x = (i % 2) * (width + padding) + (width - imgWidth) / 2;
            int y = (i / 2) * (height + padding) + (height - imgHeight) / 2;
            g.drawImage(images.get(i), x, y, null);
        }

        g.dispose();
        return mergedImage;
    }

}