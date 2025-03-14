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

    public Map<String, Object> getQrData() {
        if (qrData.containsKey("transactionAmount")) {
            qrData.put("transactionAmount", formatAmount(qrData.get("transactionAmount")));
        }
        return qrData;
    }

    private String formatAmount(Object value) {
        BigDecimal amount = new BigDecimal(value.toString());
        return String.format("%.2f", amount);  // ✅ Ensures two decimal places
    }
}


