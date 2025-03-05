package com.coop.qrcodeengine.api.controller;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
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

    @PostMapping(value = "v1.0/qrcode/generate-static", produces = MediaType.APPLICATION_JSON_VALUE)
    public String generateStaticQrCode(@RequestBody GenerateQRCodeRequest qrCodeRequest) throws Exception {
        return qrCodeRequest.getQrCode();
    }
}