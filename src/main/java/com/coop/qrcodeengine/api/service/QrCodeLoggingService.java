package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;

public interface QrCodeLoggingService {
    void logRequest(String requestId, GenerateQRCodeRequest request, String requestType);
    void updateLog(String requestId, String responseData);
}
