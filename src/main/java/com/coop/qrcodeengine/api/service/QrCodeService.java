package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeResponse;

public interface QrCodeService {
    GenerateQRCodeResponse generateStaticQrCode(GenerateQRCodeRequest qrCodeRequest) throws Exception;
    GenerateQRCodeResponse generateDynamicQrCode(GenerateQRCodeRequest qrCodeRequest) throws Exception;
}
