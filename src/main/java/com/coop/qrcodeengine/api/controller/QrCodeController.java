package com.coop.qrcodeengine.api.controller;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeResponse;
import com.coop.qrcodeengine.api.service.QrCodeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class QrCodeController {
    private static final Logger LOGGER = LogManager.getLogger(QrCodeController.class);

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @PostMapping(value = "v1.0/qrcode/generate-static", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenerateQRCodeResponse> generateStaticQrCode(@RequestBody GenerateQRCodeRequest qrCodeRequest) throws Exception {
        LOGGER.info("Received request to generate static QR code");
        GenerateQRCodeResponse response = qrCodeService.generateStaticQrCode(qrCodeRequest);
        return ResponseEntity.ok(response);
    }
}