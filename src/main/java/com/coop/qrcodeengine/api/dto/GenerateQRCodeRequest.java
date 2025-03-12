package com.coop.qrcodeengine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQRCodeRequest {
    // TODO: Make this use a hashmap to make it extensible
    private String requestType; // "GenerateStaticQrCode" or "GenerateDynamicQrCode"
    private String channelId;
    private Map<String, Object> qrData; // Flexible structure for static/dynamic QRs

    public BigDecimal getTransactionAmount() {
        Object value = qrData.get("transactionAmount");
        if (value instanceof Double) {
            return BigDecimal.valueOf((Double) value);
        } else if (value instanceof String) {
            return new BigDecimal((String) value);
        } else if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return null;
    }
}


