package com.coop.qrcodeengine.api.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class QrCodeGenerator {
    public static byte[] generateQRCodeImage(String data, int width, int height, String format) {
        try {
            if(data==null || data.isEmpty()){
                throw new IllegalArgumentException("QR code data cannot be null or empty");
            }

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean success = ImageIO.write(qrImage, format.toLowerCase(), baos); // Using WebP format for better compression
            if (!success) {
                throw new IOException("Failed to convert QR image to webp");
            }
            return baos.toByteArray();
        } catch (WriterException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write QR image", e);
        }

    }
}
