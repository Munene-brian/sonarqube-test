package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;

public interface GenerateStaticQRCode {
    String generateStaticQrCode(GenerateQRCodeRequest qrCodeRequest) throws Exception;
}
