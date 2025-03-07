package com.coop.qrcodeengine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQRCodeResponse {
    private String qrCodeImage; // Base64 encoded PNG
    private String format; // "PNG"
    private int size; // 512 (Mobile), 1024 (Web)
}
