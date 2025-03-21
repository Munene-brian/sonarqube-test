package com.coop.qrcodeengine.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class VerifyQRCodeResponse {
    private String requestMessageId;
    private Instant requestDateTime;
    private String responseCode;
    private String responseDescription;
    private String validationStatus;
    private Map<String, String> paymentRouting;
}
