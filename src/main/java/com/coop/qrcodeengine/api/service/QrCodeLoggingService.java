package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeResponse;

public interface QrCodeLoggingService {
    void logRequest(String requestId, GenerateQRCodeRequest request, String requestType);
    void updateLog(String requestId, GenerateQRCodeResponse response, String status, String statusDescription);
}
