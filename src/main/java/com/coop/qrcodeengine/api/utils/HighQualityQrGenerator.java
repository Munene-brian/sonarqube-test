package com.coop.qrcodeengine.api.utils;

import qrcode.QRCode;
import qrcode.color.QRCodeColorFunction;
import qrcode.internals.QRCodeSquare;
import qrcode.render.QRCodeGraphics;

import java.io.IOException;
import java.io.InputStream;

public class HighQualityQrGenerator {
    private static final int DARK_GREEN = 0x00513B; // Finder patterns
    private static final int LIGHT_GREEN = 0x68AB00; // Inner dots
    private static final int WHITE = 0xFFFFFF; // Background

    /**
     * Generates a high-quality QR Code with a logo using qrcode-kotlin.
     * Uses a custom color function to color the QR elements.
     *
     * @param data The content to encode in the QR code
     * @return Byte array of the QR code image
     * @throws IOException if logo loading fails
     */
    public static byte[] generateQrCodeWithLogo(String data) throws IOException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("QR code data cannot be null or empty");
        }

        // Load logo from resources
        InputStream logoStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("templates/logo.png");
        if (logoStream == null) {
            throw new IOException("Logo file not found in resources.");
        }

        byte[] logoBytes = logoStream.readAllBytes(); // Convert logo to byte array

        return QRCode
                .ofCircles()
                .withCustomColorFunction(new CustomColorFunction()) // ✅ Ensure custom color function is applied
                .withSize(7) // Default is 25
                .withLogo(logoBytes, 100, 100)  // Add logo (scaled to 100x100)
                .build(data)
                .renderToBytes();
    }

    /**
     * Implements QRCodeColorFunction to customize QR colors.
     */
    private static class CustomColorFunction implements QRCodeColorFunction {
        @Override
        public int fg(int row, int col, QRCode qrCode, QRCodeGraphics graphics) {
            return LIGHT_GREEN; // Inner dots color
        }

        @Override
        public int bg(int row, int col, QRCode qrCode, QRCodeGraphics graphics) {
            return WHITE; // Background color
        }

        @Override
        public int margin(int row, int col, QRCode qrCode, QRCodeGraphics graphics) {
            return WHITE; // Margin color
        }

        @Override
        public void beforeRender(QRCode qrCode, QRCodeGraphics graphics) {
            graphics.reset(); // Ensure QR code is initialized before rendering
        }

        @Override
        public int colorFn(QRCodeSquare square, QRCode qrCode, QRCodeGraphics graphics) {
            int x = square.absoluteX(); // Get absolute X
            int y = square.absoluteY(); // Get absolute Y
            int qrSize = qrCode.getData().length() * qrCode.getSquareSize(); // Get full QR size

            // Finder pattern check
            if (isFinderPattern(x, y, qrSize)) {
                return DARK_GREEN; // Assign finder pattern color
            }

            return LIGHT_GREEN; // Default to inner dots color
        }

        /**
         * Determines if a given (x, y) coordinate is inside a finder pattern.
         */
        private boolean isFinderPattern(int x, int y, int qrSize) {
            int finderPatternSize = 7 * (qrSize / 25); // Finder pattern size scales correctly

            return (x < finderPatternSize && y < finderPatternSize) || // Top-left
                    (x >= qrSize - finderPatternSize && y < finderPatternSize) || // Top-right
                    (x < finderPatternSize && y >= qrSize - finderPatternSize); // Bottom-left
        }
    }
}


//package com.coop.qrcodeengine.api.utils;
//
//import qrcode.QRCode;
//import qrcode.color.Colors;
//import qrcode.color.QRCodeColorFunction;
//import qrcode.internals.QRCodeSquare;
//import qrcode.render.QRCodeGraphics;
//
//import java.io.IOException;
//import java.io.InputStream;
//
//public class HighQualityQrGenerator {
//    private static final int DARK_GREEN = 0x00513B; // Corner squares and dots
//    private static final int LIGHT_GREEN = 0x68AB00; // Inner dots
//
//    /**
//     * Generates a high-quality QR Code with a logo using qrcode-kotlin.
//     * @param data The content to encode in the QR code
//     * @return Byte array of the QR code image
//     * @throws IOException if logo loading fails
//     */
//    public static byte[] generateQrCodeWithLogo(String data) throws IOException {
//        if (data == null || data.isEmpty()) {
//            throw new IllegalArgumentException("QR code data cannot be null or empty");
//        }
//
//        // Load logo from resources
//        InputStream logoStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("templates/logo.png");
//        if (logoStream == null) {
//            throw new IOException("Logo file not found in resources.");
//        }
//
//        byte[] logoBytes = logoStream.readAllBytes(); // Convert logo to byte array
//
//        return QRCode
//                .ofCircles()
//                .withCustomColorFunction(new CustomColorFunction())
//                //.withColor(Colors.css("#68AB00"))
//                //.withColor(Colors.LIGHT_GREEN)
//                //.withBackgroundColor(Colors.WHITE) // White background
//                .withSize(7) // Default is 25
//                .withLogo(logoBytes, 100, 100)  // Add logo (scaled to 50x50)
//                .build(data)
//                .renderToBytes();
//    }
//
//    /**
//     * Implements QRCodeColorFunction to customize QR colors.
//     */
//    private static class CustomColorFunction implements QRCodeColorFunction {
//        private static final int DARK_GREEN = 0x00513B; // Finder patterns
//        private static final int LIGHT_GREEN = 0x68AB00; // Inner dots
//        private static final int WHITE = 0xFFFFFF; // Background
//
//        @Override
//        public int fg(int x, int y, QRCode qrCode, QRCodeGraphics graphics) {
//            // Default foreground color (dots)
//            return LIGHT_GREEN;
//        }
//
//        @Override
//        public int bg(int x, int y, QRCode qrCode, QRCodeGraphics graphics) {
//            // Ensure background remains white
//            return WHITE;
//        }
//
//        @Override
//        public int colorFn(QRCodeSquare square, QRCode qrCode, QRCodeGraphics graphics) {
//            int x = square.absoluteX(); // Get absolute X
//            int y = square.absoluteY(); // Get absolute Y
//            int qrSize = qrCode.getData().length() * qrCode.getSquareSize(); // Get full QR size
//
//            // Debugging: Print values to verify correctness
//            System.out.println("Square Position - X: " + x + ", Y: " + y + ", QR Size: " + qrSize);
//
//
//            // Finder pattern check
//            if (isFinderPattern(x, y, qrSize)) {
//                return DARK_GREEN; // Assign finder pattern color
//            }
//
//            return LIGHT_GREEN; // Default to inner dots color
//        }
//
//        @Override
//        public int margin(int x, int y, QRCode qrCode, QRCodeGraphics graphics) {
//            // Ensure margins remain white
//            return WHITE;
//        }
//
//        @Override
//        public void beforeRender(QRCode qrCode, QRCodeGraphics graphics) {
//            // Ensure QR code is initialized before rendering
//            graphics.reset();
//        }
//
//        /**
//         * Determines if a given (x, y) coordinate is inside a finder pattern.
//         */
//        private boolean isFinderPattern(int x, int y, int qrSize) {
//            int finderPatternSize = 7 * (qrSize / 25); // // ✅ Finder pattern size scales correctly
//
//            return (x < finderPatternSize && y < finderPatternSize) || // Top-left
//                    (x >= qrSize - finderPatternSize && y < finderPatternSize) || // Top-right
//                    (x < finderPatternSize && y >= qrSize - finderPatternSize); // Bottom-left
//        }
//    }
//
//
//    /**
//     * Custom color function for differentiating QR elements.
//     * - Dark Green (`#00513B`) for **finder patterns (corner squares & dots)**.
//     * - Light Green (`#68AB00`) for **inner dots**.
//     */
////    private static int customColorFunction(int x, int y, boolean isFinderPattern) {
////        return isFinderPattern ? DARK_GREEN : LIGHT_GREEN;
////    }
//}
