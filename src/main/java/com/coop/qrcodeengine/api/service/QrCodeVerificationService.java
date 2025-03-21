package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.dto.VerifyQRCodeRequest;
import com.coop.qrcodeengine.api.dto.VerifyQRCodeResponse;

public interface QrCodeVerificationService {
    VerifyQRCodeResponse verifyQrCode(VerifyQRCodeRequest request);
}
