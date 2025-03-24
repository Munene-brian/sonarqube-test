package com.coop.qrcodeengine.api.utils;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QrImageGenerator {
    private static final int QR_SIZE = 400;
    private static final double QUIET_ZONE = 1.5;
    private static final int FINDER_PATTERN_SIZE = 7;

    private static final Color DARK_GREEN = new Color(0x00513B);
    private static final Color LIGHT_GREEN = new Color(0x68AB00);

    public static byte[] generateStyledQRCode(String data, String format, byte[] logoImage) throws IOException {
        try {
            if (data == null || data.isEmpty()) {
                throw new IllegalArgumentException("QR code data cannot be null or empty");
            }

            // Encode QR Code using ZXing
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
//            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            QRCode qrCode = Encoder.encode(data, ErrorCorrectionLevel.H, hints);
            BufferedImage qrImage = renderQRImage(qrCode);

            // Overlay logo at the center
            overlayLogo(qrImage, logoImage);

            // Convert BufferedImage to byte array
            return convertImageToByteArray(qrImage, format);

        } catch (WriterException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    private static BufferedImage renderQRImage(QRCode code) {
        BufferedImage image = new BufferedImage(QrImageGenerator.QR_SIZE, QrImageGenerator.QR_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setColor(LIGHT_GREEN);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setBackground(Color.white);
        graphics.clearRect(0, 0, QrImageGenerator.QR_SIZE, QrImageGenerator.QR_SIZE);

        ByteMatrix input = code.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }

        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        // QR width and height including quiet zone
        int qrWidth = inputWidth + (int)(QUIET_ZONE * 2);
        int qrHeight = inputHeight + (int)(QUIET_ZONE * 2);
//        int multiple = Math.min(QrImageGenerator.QR_SIZE / qrWidth, QrImageGenerator.QR_SIZE / qrHeight);
        // 1. Compute the correct scaling factor (excluding QUIET_ZONE)
        int multiple = Math.min(QrImageGenerator.QR_SIZE / qrWidth,
                QrImageGenerator.QR_SIZE / qrHeight);
        // 2. Compute correct padding (ensuring QUIET_ZONE is not applied twice)
        int leftPadding = (QrImageGenerator.QR_SIZE - (inputWidth * multiple)) / 2;
        int topPadding = (QrImageGenerator.QR_SIZE - (inputHeight * multiple)) / 2;

        graphics.setColor(LIGHT_GREEN);
        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (input.get(inputX, inputY) == 1) {
                    graphics.fillRoundRect(outputX, outputY, multiple, multiple, multiple / 5, multiple / 5);
//                    graphics.fillRect(outputX, outputY, multiple, multiple);
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

    private static void drawFinderPattern(Graphics2D graphics, int x, int y, int size) {
        final int INNER_SQUARE_SIZE = size * 5 / 7;
        final int INNER_SQUARE_OFFSET = size / 7;
        final int CENTER_SQUARE_SIZE = size * 3 / 7;
        final int CENTER_SQUARE_OFFSET = size * 2 / 7;
        final int ROUNDED_CORNER_ARC = (int) (size / 1.5); // Ensure consistent rounding
        final int INNER_CORNER_ARC = size / 4;
        final int BORDER_SIZE = size / 20; // Thin border

        // Clear the background before drawing
        graphics.setColor(Color.WHITE);
        graphics.fillRect(x, y, size, size);

        // Outer dark green rounded square
        graphics.setColor(DARK_GREEN);
        graphics.fillRoundRect(x + BORDER_SIZE, y + BORDER_SIZE, size - 2 * BORDER_SIZE, size - 2 * BORDER_SIZE, ROUNDED_CORNER_ARC, ROUNDED_CORNER_ARC);


        // Middle white rounded square
        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(
                x + INNER_SQUARE_OFFSET, y + INNER_SQUARE_OFFSET,
                INNER_SQUARE_SIZE, INNER_SQUARE_SIZE,
                ROUNDED_CORNER_ARC, ROUNDED_CORNER_ARC
        );

        // Inner dark green rounded square (center)
        graphics.setColor(DARK_GREEN);
        graphics.fillRoundRect(x + CENTER_SQUARE_OFFSET, y + CENTER_SQUARE_OFFSET, CENTER_SQUARE_SIZE, CENTER_SQUARE_SIZE, INNER_CORNER_ARC, INNER_CORNER_ARC);
    }

    private static void overlayLogo(BufferedImage qrImage, byte[] logoImage) throws IOException {
//        InputStream logoStream = new ClassPathResource("templates/logo.png").getInputStream();
        if (logoImage == null) {
            return; // Skip overlay if no logo is provided
        }

        BufferedImage logo = ImageIO.read(new ByteArrayInputStream(logoImage));

        // Ensure logo is in high-quality ARGB format
        BufferedImage highQualityLogo = new BufferedImage(logo.getWidth(), logo.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = highQualityLogo.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC); // Correct constant
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(logo, 0, 0, logo.getWidth(), logo.getHeight(), null);
        g2d.dispose();

        int logoSize = qrImage.getWidth() / 6; // Scale logo dynamically
        int logoX = (qrImage.getWidth() - logoSize) / 2;
        int logoY = (qrImage.getHeight() - logoSize) / 2;

        Graphics2D g = qrImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Clear a small white background area for the logo
        int padding = logoSize / 8; // Adjust for cleaner spacing
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