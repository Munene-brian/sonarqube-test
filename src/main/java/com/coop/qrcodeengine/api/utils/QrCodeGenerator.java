package com.coop.qrcodeengine.api.utils;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class QrCodeGenerator {
    private static final int QR_SIZE = 400;
    private static final int QUIET_ZONE = 4;
    private static final int FINDER_PATTERN_SIZE = 7;

    private static final Color DARK_GREEN = new Color(0x00513B);
    private static final Color LIGHT_GREEN = new Color(0x68AB00);

    public static byte[] generateStyledQRCode(String data, String format) throws IOException {
        try {
            if (data == null || data.isEmpty()) {
                throw new IllegalArgumentException("QR code data cannot be null or empty");
            }

            // Encode QR Code using ZXing
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            QRCode qrCode = Encoder.encode(data, ErrorCorrectionLevel.H, hints);
            BufferedImage qrImage = renderQRImage(qrCode);

            // Overlay logo at the center
            overlayLogo(qrImage);

            // Convert BufferedImage to byte array
            return convertImageToByteArray(qrImage, format);

        } catch (WriterException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    private static BufferedImage renderQRImage(QRCode code) {
        BufferedImage image = new BufferedImage(QrCodeGenerator.QR_SIZE, QrCodeGenerator.QR_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setBackground(Color.white);
        graphics.clearRect(0, 0, QrCodeGenerator.QR_SIZE, QrCodeGenerator.QR_SIZE);

        ByteMatrix input = code.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }

        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth + (QrCodeGenerator.QUIET_ZONE * 2);
        int qrHeight = inputHeight + (QrCodeGenerator.QUIET_ZONE * 2);
        int multiple = Math.min(QrCodeGenerator.QR_SIZE / qrWidth, QrCodeGenerator.QR_SIZE / qrHeight);
        int leftPadding = (QrCodeGenerator.QR_SIZE - (inputWidth * multiple)) / 2;
        int topPadding = (QrCodeGenerator.QR_SIZE - (inputHeight * multiple)) / 2;

        graphics.setColor(LIGHT_GREEN);
        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (input.get(inputX, inputY) == 1) {
                    graphics.fillRect(outputX, outputY, multiple, multiple);
                }
            }
        }

        int squareSize = multiple * FINDER_PATTERN_SIZE;
        drawFinderPattern(graphics, leftPadding, topPadding, squareSize);
        drawFinderPattern(graphics, leftPadding + (inputWidth - FINDER_PATTERN_SIZE) * multiple, topPadding, squareSize);
        drawFinderPattern(graphics, leftPadding, topPadding + (inputHeight - FINDER_PATTERN_SIZE) * multiple, squareSize);

        graphics.dispose();
        return image;
    }


//    private static BufferedImage renderQRImage(QRCode code, int width, int height, int quietZone) {
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D graphics = image.createGraphics();
//
//        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        graphics.setBackground(Color.white);
//        graphics.clearRect(0, 0, width, height);
//
//        ByteMatrix input = code.getMatrix();
//        if (input == null) {
//            throw new IllegalStateException();
//        }
//
//        int inputWidth = input.getWidth();
//        int inputHeight = input.getHeight();
//        int qrWidth = inputWidth + (quietZone * 2);
//        int qrHeight = inputHeight + (quietZone * 2);
//        int multiple = Math.min(width / qrWidth, height / qrHeight);
//        int leftPadding = (width - (inputWidth * multiple)) / 2;
//        int topPadding = (height - (inputHeight * multiple)) / 2;
//
//        // Define logo placement area (center)
//        int logoSize = width / 5;
//        int logoX = (width - logoSize) / 2;
//        int logoY = (height - logoSize) / 2;
//
//        graphics.setColor(LIGHT_GREEN);
//        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
//            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
//                if (input.get(inputX, inputY) == 1) {
//                    // Define padding around the logo
//                    // Adjust as needed
//
//                    // Skip drawing dots inside the logo area
////                    if (outputX > logoX - multiple && outputX < logoX + logoSize + multiple &&
////                            outputY > logoY - multiple && outputY < logoY + logoSize + multiple) {
////                        continue; // Skip this dot to create clear space
////                    }
//                    // Skip drawing in the logo area
//                    if (outputX >= logoX && outputX < logoX + logoSize &&
//                            outputY >= logoY && outputY < logoY + logoSize) {
//                        continue;
//                    }
//                    graphics.fillRect(outputX, outputY, multiple, multiple);
//                }
//            }
//        }
//
//        int squareSize = multiple * FINDER_PATTERN_SIZE;
//        drawFinderPattern(graphics, leftPadding, topPadding, squareSize);
//        drawFinderPattern(graphics, leftPadding + (inputWidth - FINDER_PATTERN_SIZE) * multiple, topPadding, squareSize);
//        drawFinderPattern(graphics, leftPadding, topPadding + (inputHeight - FINDER_PATTERN_SIZE) * multiple, squareSize);
//
//        graphics.dispose();
//        return image;
//    }

    private static void drawFinderPattern(Graphics2D graphics, int x, int y, int size) {
        final int INNER_SQUARE_SIZE = size * 5 / 7;
        final int INNER_SQUARE_OFFSET = size / 7;
        final int CENTER_SQUARE_SIZE = size * 3 / 7;
        final int CENTER_SQUARE_OFFSET = size * 2 / 7;
        final int ROUNDED_CORNER_ARC = size / 4;

        // Clear the background around the finder pattern before drawing
        graphics.setColor(Color.WHITE);
        graphics.fillRect(x, y, size, size); // Ensures no background interference

        graphics.setColor(DARK_GREEN);
        graphics.fillRoundRect(x, y, size, size, ROUNDED_CORNER_ARC, ROUNDED_CORNER_ARC);
        graphics.setColor(Color.white);
        graphics.fillRoundRect(x + INNER_SQUARE_OFFSET, y + INNER_SQUARE_OFFSET, INNER_SQUARE_SIZE, INNER_SQUARE_SIZE, ROUNDED_CORNER_ARC, ROUNDED_CORNER_ARC);
        graphics.setColor(DARK_GREEN);
        graphics.fillRoundRect(x + CENTER_SQUARE_OFFSET, y + CENTER_SQUARE_OFFSET, CENTER_SQUARE_SIZE, CENTER_SQUARE_SIZE, ROUNDED_CORNER_ARC, ROUNDED_CORNER_ARC);
    }

    private static void overlayLogo(BufferedImage qrImage) throws IOException {
        InputStream logoStream = new ClassPathResource("templates/logo.png").getInputStream();

        BufferedImage logo = ImageIO.read(logoStream);

        // Ensure logo is in high-quality ARGB format
        BufferedImage highQualityLogo = new BufferedImage(logo.getWidth(), logo.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = highQualityLogo.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC); // Correct constant
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(logo, 0, 0, logo.getWidth(), logo.getHeight(), null);
        g2d.dispose();

        int logoSize = qrImage.getWidth() / 7; // Scale logo dynamically
        int logoX = (qrImage.getWidth() - logoSize) / 2;
        int logoY = (qrImage.getHeight() - logoSize) / 2;

        Graphics2D g = qrImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Clear a small white background area for the logo
        int padding = logoSize / 10; // Adjust for cleaner spacing
        g.setColor(Color.WHITE);
        g.fillRoundRect(logoX - padding, logoY - padding, logoSize + (2 * padding), logoSize + (2 * padding), 20, 20);

        // Draw the high-quality logo
        g.drawImage(highQualityLogo, logoX, logoY, logoSize, logoSize, null);
        g.dispose();

    }

    private static byte[] convertImageToByteArray(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean success = ImageIO.write(image, format.toLowerCase(), baos);
        if (!success) {
            throw new IOException("Failed to convert QR image to " + format);
        }
        return baos.toByteArray();
    }
}