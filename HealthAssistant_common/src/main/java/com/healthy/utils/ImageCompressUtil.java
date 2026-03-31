package com.healthy.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageCompressUtil {

    /**
     * 压缩图片到指定最大大小（单位：字节）
     * @param imageBytes 原始图片字节数组
     * @param maxSize 最大允许大小（字节），例如 4 * 1024 * 1024 = 4MB
     * @return 压缩后的图片字节数组，如果原始已小于maxSize则直接返回原数组
     * @throws IOException
     */
    public static byte[] compressToMaxSize(byte[] imageBytes, long maxSize) throws IOException {
        if (imageBytes.length <= maxSize) {
            return imageBytes;
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        BufferedImage image = ImageIO.read(bis);
        if (image == null) {
            throw new IOException("无法读取图片");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // 计算压缩比例：按面积比例缩放，估算文件大小与面积成正比
        double ratio = Math.sqrt((double) maxSize / imageBytes.length);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        // 如果新尺寸太小，设置最小尺寸
        newWidth = Math.max(newWidth, 100);
        newHeight = Math.max(newHeight, 100);

        // 创建缩放后的图片
        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        // 转换为JPEG格式（可以调整质量）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(outputImage, "jpg", baos);
        byte[] compressedBytes = baos.toByteArray();

        // 如果仍然大于maxSize，递归压缩（或者调整质量参数）
        if (compressedBytes.length > maxSize) {
            // 简单处理：再次压缩，或者使用有损压缩参数
            return compressToMaxSize(compressedBytes, maxSize);
        }
        return compressedBytes;
    }
}