package com.coop.qrcodeengine.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class VerifyQRCodeRequest {
    @NotBlank
    private String requestMessageId;
    @NotNull
    private Instant requestDateTime;
    @NotBlank
    private String requestType;
    @NotBlank
    private String channelId;

    private String qrCodeData;
    private String qrCodeImage;
}
