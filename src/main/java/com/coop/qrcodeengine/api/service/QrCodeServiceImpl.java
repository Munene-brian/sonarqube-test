package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeResponse;
import com.coop.qrcodeengine.api.exception.QrCodeGenerationException;
import com.coop.qrcodeengine.api.utils.QrCodeGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class QrCodeServiceImpl implements QrCodeService {
    private static final Logger LOGGER = LogManager.getLogger(QrCodeServiceImpl.class);

    public GenerateQRCodeResponse generateStaticQrCode(GenerateQRCodeRequest qrCodeRequest) {
        try {
            LOGGER.info("Generating static QR code for " + qrCodeRequest.getQrCode());

            String format = "PNG";
            int size = 512;

            byte[] qrImage = QrCodeGenerator.generateQRCodeImage(qrCodeRequest.getQrCode(), size, size, format);
            String base64Qr = Base64.getEncoder().encodeToString(qrImage);

            return new GenerateQRCodeResponse(base64Qr, format, size);
        }
        catch (Exception e) {
            LOGGER.error("Error generating QR Code", e);
            throw new QrCodeGenerationException("Failed to generate QR Code");
        }
    }
}
