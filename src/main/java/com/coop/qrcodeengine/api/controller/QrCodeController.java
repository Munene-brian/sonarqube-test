package com.coop.qrcodeengine.api.controller;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeResponse;
import com.coop.qrcodeengine.api.dto.VerifyQRCodeRequest;
import com.coop.qrcodeengine.api.dto.VerifyQRCodeResponse;
import com.coop.qrcodeengine.api.service.QrCodeService;
import com.coop.qrcodeengine.api.service.QrCodeVerificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1.0/qrcode")
public class QrCodeController {
    private static final Logger LOGGER = LogManager.getLogger(QrCodeController.class);

    private final QrCodeService qrCodeService;
    private final QrCodeVerificationService qrCodeVerificationService;

    public QrCodeController(QrCodeService qrCodeService, QrCodeVerificationService qrCodeVerificationService) {
        this.qrCodeService = qrCodeService;
        this.qrCodeVerificationService = qrCodeVerificationService;
    }

    @PostMapping(value = "generate-static", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenerateQRCodeResponse> generateStaticQrCode(@RequestBody GenerateQRCodeRequest qrCodeRequest) throws Exception {
        LOGGER.info("Received request to generate static QR code");
        GenerateQRCodeResponse response = qrCodeService.generateStaticQrCode(qrCodeRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "generate-dynamic", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenerateQRCodeResponse> generateDynamicQr(@RequestBody GenerateQRCodeRequest request) throws Exception {
        return ResponseEntity.ok(qrCodeService.generateDynamicQrCode(request));
    }

    @PostMapping("verify")
    public ResponseEntity<VerifyQRCodeResponse> verifyQrCode(@RequestBody VerifyQRCodeRequest request) {
        VerifyQRCodeResponse response = qrCodeVerificationService.verifyQrCode(request);
        return ResponseEntity.ok(response);
    }
}