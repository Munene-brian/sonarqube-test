package com.coop.qrcodeengine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQRCodeResponse {
    private String qrCodeImage; // Base64 encoded WebP
    private String format; // "WEBP"
    private int size; // 512 (Mobile)
}
