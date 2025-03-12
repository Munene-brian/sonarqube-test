package com.coop.qrcodeengine.api.utils;

import com.coop.qrcodeengine.api.entity.DynamicQrCodeTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class QrCodeBuilder {
    /**
     * Builds a QR Code Data String dynamically using the TLV (Tag-Length-Value) format.
     * Supports both Static and Dynamic QR Code templates.
     * @param templateList List of QR templates from DB
     * @param qrData Map containing key-value pairs of QR fields
     * @param isDynamic Determines if it's a static or dynamic QR
     * @return QR Code string formatted with TLV
     */
    public static String buildQRCodeData(List<DynamicQrCodeTemplate> templateList, Map<String, Object> qrData, boolean isDynamic) {
        StringBuilder qrDataString = new StringBuilder();

//        // 1️⃣ Add mandatory fields for CBK compliance
//        qrDataString.append("00").append("02").append("01"); // Payload Format Indicator
//        qrDataString.append("01").append("02").append(isDynamic ? "12" : "11"); // Static (11) or Dynamic (12) QR

        // 2️⃣ Sort template by tag ID to maintain proper structure
        templateList.sort(Comparator.comparingInt(DynamicQrCodeTemplate::getId));

        // 3️⃣ Append template-based and user-provided values
        for (DynamicQrCodeTemplate template : templateList) {
            String tag = String.format("%02d", template.getId());
            String value = getValueForTag(template, qrData, isDynamic);

            // 4️⃣ Handle null values based on 'required' flag
            if (value == null) {
                if (!"63".equals(tag) && "1".equals(String.valueOf(template.getRequired()))) {
                    throw new RuntimeException("Required field with tag " + tag + " is missing.");
                } else {
                    continue; // Skip optional fields if null
                }
            }

            String length = String.format("%02d", value.length());
            qrDataString.append(tag).append(length).append(value);
        }

        // 5️⃣ Compute CRC for CBK standards and append at the end
        String crc = CRCUtils.computeCRC(qrDataString.toString());
        qrDataString.append("63").append("04").append(crc);

        return qrDataString.toString();
    }

    /**
     * Extracts values from qrData hashmap, fallback to DB values
     * @param template Static/Dynamic QR template field
     * @param qrData Input data from request
     * @param isDynamic Indicates if it's a dynamic QR
     * @return Correct value to use for the field
     */
    private static String getValueForTag(DynamicQrCodeTemplate template, Map<String, Object> qrData, boolean isDynamic) {
        return switch (String.valueOf(template.getId())) {
            case "1" -> isDynamic ? "12" : "11";
            case "59" -> (String) qrData.get("merchantName"); // Merchant Name
            case "60" -> (String) qrData.get("merchantCity"); // Merchant City
            case "61" -> (String) qrData.get("postalCode"); // Postal Code
            case "29" -> buildMerchantAccountInfo((String) qrData.get("merchantAccountInformation")); // Merchant Account Info
            case "52" -> (String) qrData.get("merchantCategoryCode"); // Merchant Category Code
            //case "53" -> qrData.containsKey("transactionCurrency") ? qrData.get("transactionCurrency").toString() : null;
            case "54" -> isDynamic ? formatAmountForTLV(qrData.get("transactionAmount")) : null;
            //case "54" -> isDynamic ? formatBigDecimal(convertToBigDecimal(qrData.get("transactionAmount"))) : null;
            //case "54" -> isDynamic ? qrData.get("transactionAmount").toString() : null; // Amount only for dynamic
            case "82" -> "01" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm")); // Timestamp
            default -> template.getContentValue(); // Use DB default value
        };
    }

    /**
     * Constructs Merchant Account Information using CBK standard format
     * @param merchantAccount Merchant's account identifier
     * @return Properly formatted Merchant Account Information TLV
     */
    private static String buildMerchantAccountInfo(String merchantAccount) {
        String qrSubDomain = "ke.go.qr"; // Example, should be fetched from config

        // Construct KE QR Sub-Domain TLV
        String keQrSubDomainTag = "00";
        String keQrSubDomainLength = String.format("%02d", qrSubDomain.length());
        String keQrSubDomainTLV = keQrSubDomainTag + keQrSubDomainLength + qrSubDomain;

        // Construct PSP ID TLV
        String pspIdTag = "11";
        String pspIdLength = String.format("%02d", merchantAccount.length());
        String pspIdTLV = pspIdTag + pspIdLength + merchantAccount;

        // Construct Merchant Account Info TLV
        String merchantAccountTag = "29";
        String merchantAccountValue = keQrSubDomainTLV + pspIdTLV;
        String merchantAccountLength = String.format("%02d", merchantAccountValue.length());

        return merchantAccountTag + merchantAccountLength + merchantAccountValue;
    }

    // ✅ Properly formats BigDecimal for QR Code TLV standard
    private static String formatBigDecimal(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }

    private static BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO; // Default to 0 if null
        } else if (value instanceof BigDecimal) {
            return (BigDecimal) value; // Already BigDecimal
        } else if (value instanceof Double) {
            return BigDecimal.valueOf((Double) value); // Convert Double → BigDecimal
        } else if (value instanceof String) {
            return new BigDecimal((String) value); // Convert String → BigDecimal
        } else {
            throw new IllegalArgumentException("Invalid type for transactionAmount: " + value.getClass().getName());
        }
    }

    private static String formatAmountForTLV(Object value) {
        BigDecimal amount = convertToBigDecimal(value);
        return String.format("%.2f", amount); // 1000.00
    }


}
