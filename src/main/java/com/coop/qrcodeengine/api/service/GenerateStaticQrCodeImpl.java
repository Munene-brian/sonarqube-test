package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class GenerateStaticQrCodeImpl implements GenerateStaticQRCode {
    private static final Logger logger = LogManager.getLogger(GenerateStaticQrCodeImpl.class);

    public String generateStaticQrCode(GenerateQRCodeRequest qrCodeRequest) {
        return qrCodeRequest.getQrCode();
    }
}
