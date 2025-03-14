package com.coop.qrcodeengine.api.utils;

import com.coop.qrcodeengine.api.entity.QrTlvTemplate;
import com.coop.qrcodeengine.api.entity.QrTlvSubtemplate;
import com.coop.qrcodeengine.api.repository.QrTlvSubtemplateRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class QrCodeBuilder {
    private final QrTlvSubtemplateRepository qrTlvSubtemplateRepository;

    public QrCodeBuilder(QrTlvSubtemplateRepository qrTlvSubtemplateRepository) {
        this.qrTlvSubtemplateRepository = qrTlvSubtemplateRepository;
    }

    public String buildQRCodeData(List<QrTlvTemplate> templateList, Map<String, Object> qrData, boolean isDynamic) {
        StringBuilder qrDataString = new StringBuilder();
        templateList.sort(Comparator.comparing(a -> a.getId().getTagId()));

        // Preload all subtemplates for optimization
        Map<Integer, List<QrTlvSubtemplate>> subTemplateMap = preloadSubTemplates();

        for (QrTlvTemplate template : templateList) {
            String tag = String.format("%02d", template.getId().getTagId());
            String value = getValueForTag(template, qrData, isDynamic, subTemplateMap);

            List<QrTlvSubtemplate> subTemplates = subTemplateMap.getOrDefault(template.getId().getTagId(), Collections.emptyList());
            String nestedSubTemplate = buildNestedTags(subTemplates, qrData, isDynamic, subTemplateMap);

            if (!nestedSubTemplate.isEmpty()) {
                value = nestedSubTemplate;
            }

            if (value == null) {
                if (!"63".equals(tag) && "1".equals(String.valueOf(template.getRequired()))) {
                    throw new RuntimeException("Required field with tag " + tag + " is missing.");
                } else {
                    continue;
                }
            }

            String length = String.format("%02d", value.length());
            qrDataString.append(tag).append(length).append(value);
        }

        String crc = CRCUtils.computeCRC(qrDataString.toString());
        qrDataString.append("63").append("04").append(crc);

        return qrDataString.toString();
    }

    private String buildNestedTags(List<QrTlvSubtemplate> subTemplates, Map<String, Object> qrData, boolean isDynamic,
                                   Map<Integer, List<QrTlvSubtemplate>> subTemplateMap) {
        if (subTemplates == null || subTemplates.isEmpty()) {
            return "";
        }

        StringBuilder nestedString = new StringBuilder();
        subTemplates.sort(Comparator.comparing(QrTlvSubtemplate::getSubTagId));

        for (QrTlvSubtemplate subTemplate : subTemplates) {
            String tag = String.format("%02d", subTemplate.getSubTagId());
            String value;

            // ✅ Process each timestamp subtag (Tag 82) separately
            if (subTemplate.getQrTlvTemplate() != null && subTemplate.getQrTlvTemplate().getId().getTagId() == 82) {
                value = getTimestampSubtagValue(subTemplate);
            } else {
                value = getValueFromTemplateOrUser(subTemplate, qrData, isDynamic, subTemplateMap);
            }

            if (value == null) {
                continue;
            }

            List<QrTlvSubtemplate> childSubTemplates = subTemplateMap.getOrDefault(subTemplate.getSubTagId(), Collections.emptyList());
            String nestedChildSubTemplate = buildNestedTags(childSubTemplates, qrData, isDynamic, subTemplateMap);

            if (!nestedChildSubTemplate.isEmpty()) {
                value += nestedChildSubTemplate;
            }

            String length = String.format("%02d", value.length());
            nestedString.append(tag).append(length).append(value);
        }

        return nestedString.toString();
    }


//    private String buildNestedTags(List<QrTlvSubtemplate> subTemplates, Map<String, Object> qrData, boolean isDynamic,
//                                   Map<Integer, List<QrTlvSubtemplate>> subTemplateMap) {
//        if (subTemplates == null || subTemplates.isEmpty()) {
//            return "";
//        }
//
//        StringBuilder nestedString = new StringBuilder();
//        subTemplates.sort(Comparator.comparing(QrTlvSubtemplate::getSubTagId));
//
//        for (QrTlvSubtemplate subTemplate : subTemplates) {
//            String tag = String.format("%02d", subTemplate.getSubTagId());
//            String value;
//
//            // ✅ Handle timestamp fields correctly inside nested processing
//            if (subTemplate.getQrTlvTemplate() != null && subTemplate.getQrTlvTemplate().getId().getTagId() == 82) {
//                value = buildTimestampTag(subTemplateMap);
//            } else {
//                value = getValueFromTemplateOrUser(subTemplate, qrData, isDynamic, subTemplateMap);
//            }
//
//            if (value == null) {
//                continue;
//            }
//
//            List<QrTlvSubtemplate> childSubTemplates = subTemplateMap.getOrDefault(subTemplate.getSubTagId(), Collections.emptyList());
//            String nestedChildSubTemplate = buildNestedTags(childSubTemplates, qrData, isDynamic, subTemplateMap);
//
//            if (!nestedChildSubTemplate.isEmpty()) {
//                value += nestedChildSubTemplate;
//            }
//
//            String length = String.format("%02d", value.length());
//            nestedString.append(tag).append(length).append(value);
//        }
//
//        return nestedString.toString();
//    }

    private Map<Integer, List<QrTlvSubtemplate>> preloadSubTemplates() {
        List<QrTlvSubtemplate> allSubTemplates = qrTlvSubtemplateRepository.findAll();
        Map<Integer, List<QrTlvSubtemplate>> subTemplateMap = new HashMap<>();

        for (QrTlvSubtemplate subTemplate : allSubTemplates) {
            // Ensure parent template mapping is correct (Handles direct nesting like 82 → 0,1,2)
            if (subTemplate.getQrTlvTemplate() != null) {
                int parentTemplateTagId = subTemplate.getQrTlvTemplate().getId().getTagId();
                subTemplateMap.computeIfAbsent(parentTemplateTagId, k -> new ArrayList<>()).add(subTemplate);
            }

            // Ensure sub-template mapping is correct (Handles deeper nesting like 11 inside 29)
            if (subTemplate.getParentSubTag() != null) {
                int parentSubTagId = subTemplate.getParentSubTag().getSubTagId();
                subTemplateMap.computeIfAbsent(parentSubTagId, k -> new ArrayList<>()).add(subTemplate);
            }
        }

        return subTemplateMap;
    }


//    private Map<Integer, List<QrTlvSubtemplate>> preloadSubTemplates() {
//        List<QrTlvSubtemplate> allSubTemplates = qrTlvSubtemplateRepository.findAll();
////        return allSubTemplates.stream()
////                .filter(subTemplate -> subTemplate.getQrTlvTemplate() != null)
////                .collect(Collectors.groupingBy(subTemplate -> subTemplate.getQrTlvTemplate().getId().getTagId()));
//        // **Fix: Ensure both parent TEMPLATE_ID and PARENT_SUB_TAG_ID are considered**
//        Map<Integer, List<QrTlvSubtemplate>> subTemplateMap = new HashMap<>();
//
//        for (QrTlvSubtemplate subTemplate : allSubTemplates) {
//            if (subTemplate.getQrTlvTemplate() != null) {
//                int parentTagId = subTemplate.getQrTlvTemplate().getId().getTagId();
//                subTemplateMap.computeIfAbsent(parentTagId, k -> new ArrayList<>()).add(subTemplate);
//            }
//
//            if (subTemplate.getParentSubTag() != null) {
//                int parentSubTagId = subTemplate.getParentSubTag().getSubTagId();
//                subTemplateMap.computeIfAbsent(parentSubTagId, k -> new ArrayList<>()).add(subTemplate);
//            }
//        }
//
//        return subTemplateMap;
//    }

    private static String getValueForTag(QrTlvTemplate template, Map<String, Object> qrData, boolean isDynamic, Map<Integer, List<QrTlvSubtemplate>> subTemplateMap) {
        // Special handling for qr type field (Tag 01)
        if (template.getId().getTagId() == 1) {
            return isDynamic ? "12" : "11";
        }

        if (template.getContentValue() != null) {
            return template.getContentValue();
        }

//        // Special handling for timestamp field (Tag 82)
//        if (template.getId().getTagId() == 82) {
//            return buildTimestampTag(template, subTemplateMap);
//        }

//        return qrData.getOrDefault(template.getContentDesc(), null) != null ? qrData.get(template.getContentDesc()).toString() : null;
        return qrData.getOrDefault(template.getJsonKey(), null) != null
                ? qrData.get(template.getJsonKey()).toString()
                : null;

    }

    private static String getValueFromTemplateOrUser(QrTlvSubtemplate subTemplate, Map<String, Object> qrData, boolean isDynamic, Map<Integer, List<QrTlvSubtemplate>> subTemplateMap) {
//        if (subTemplate.getContentValue() != null) {
//            return subTemplate.getContentValue();
//        }
////        return qrData.getOrDefault(subTemplate.getContentDesc(), null) != null ? qrData.get(subTemplate.getContentDesc()).toString() : null;
//        return qrData.getOrDefault(subTemplate.getJsonKey(), null) != null
//                ? qrData.get(subTemplate.getJsonKey()).toString()
//                : null;
        // **Fix: Check if this sub-template has child sub-templates**
        List<QrTlvSubtemplate> childSubTemplates = subTemplateMap.getOrDefault(subTemplate.getSubTagId(), Collections.emptyList());

        if (!childSubTemplates.isEmpty()) {
            return ""; // Placeholder; the nested structure will be handled in `buildNestedTags`
        }

        if (subTemplate.getContentValue() != null) {
            return subTemplate.getContentValue();
        }

        return qrData.getOrDefault(subTemplate.getJsonKey(), null) != null
                ? qrData.get(subTemplate.getJsonKey()).toString()
                : null;
    }

    private static String formatAmountForTLV(Object value) {
        BigDecimal amount = new BigDecimal(value.toString());
        return String.format("%.2f", amount);
    }

    private static String getTimestampSubtagValue(QrTlvSubtemplate subTemplate) {
        String value;

        if (subTemplate.getSubTagId() == 0) {
            value = subTemplate.getContentValue();  // ke.go.qr
        } else if (subTemplate.getSubTagId() == 1) {
            value = TimeUtils.generateQrTimestamp(); // Current timestamp
        } else if (subTemplate.getSubTagId() == 2) {
            value = subTemplate.getContentValue();  // Expiration time (future use)
        } else {
            value = subTemplate.getContentValue();
        }

        return value;
    }


    private static String buildTimestampTag(Map<Integer, List<QrTlvSubtemplate>> subTemplateMap) {
        StringBuilder timestampTag = new StringBuilder();
        List<QrTlvSubtemplate> subTemplates = subTemplateMap.getOrDefault(82, Collections.emptyList());

        for (QrTlvSubtemplate subTemplate : subTemplates) {
            String tag = String.format("%02d", subTemplate.getSubTagId());
            String value;

            if (subTemplate.getSubTagId() == 0) {
                value = subTemplate.getContentValue();  // ke.go.qr
            } else if (subTemplate.getSubTagId() == 1) {
                value = TimeUtils.generateQrTimestamp(); // Current timestamp
            } else if (subTemplate.getSubTagId() == 2) {
                value = subTemplate.getContentValue();  // Expiration time (future use)
            } else {
                value = subTemplate.getContentValue();
            }

            if (value != null) {
                String length = String.format("%02d", value.length());
                timestampTag.append(tag).append(length).append(value);
            }
        }

        return timestampTag.toString();
    }


//    private static String buildTimestampTag(QrTlvTemplate template, Map<Integer, List<QrTlvSubtemplate>> subTemplateMap) {
//        StringBuilder timestampTag = new StringBuilder();
//
//        // Load nested sub-tags dynamically
//        List<QrTlvSubtemplate> subTemplates = subTemplateMap.getOrDefault(template.getId().getTagId(), Collections.emptyList());
//
//        for (QrTlvSubtemplate subTemplate : subTemplates) {
//            String tag = String.format("%02d", subTemplate.getSubTagId());
//            String value;
//
//            // If tag 00 (Globally Unique Identifier), get content from DB
//            if (subTemplate.getSubTagId() == 0) {
//                value = subTemplate.getContentValue();
//            }
//            // If tag 01 (Generation Time), generate timestamp dynamically
//            else if (subTemplate.getSubTagId() == 1) {
//                value = TimeUtils.generateQrTimestamp();
//            }
//            // If tag 02 (Expiration Time) is introduced in the future, it will follow the same logic
//            else {
//                value = subTemplate.getContentValue(); // Placeholder for future tags
//            }
//
//            if (value != null) {
//                String length = String.format("%02d", value.length());
//                timestampTag.append(tag).append(length).append(value);
//            }
//        }
//
//        return timestampTag.toString();
//    }



}







//package com.coop.qrcodeengine.api.utils;
//
//import com.coop.qrcodeengine.api.entity.QrTlvTemplate;
//import com.coop.qrcodeengine.api.entity.QrTlvSubtemplate;
//import com.coop.qrcodeengine.api.repository.QrTlvSubtemplateRepository;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@Component
//public class QrCodeBuilder {
//    private final QrTlvSubtemplateRepository qrTlvSubtemplateRepository;
//
//    public QrCodeBuilder(QrTlvSubtemplateRepository qrTlvSubtemplateRepository) {
//        this.qrTlvSubtemplateRepository = qrTlvSubtemplateRepository;
//    }
//
//    public String buildQRCodeData(List<QrTlvTemplate> templateList, Map<String, Object> qrData, boolean isDynamic) {
//        StringBuilder qrDataString = new StringBuilder();
//
//        templateList.sort(Comparator.comparing(a -> a.getId().getTagId()));
//
//        for (QrTlvTemplate template : templateList) {
//            String tag = String.format("%02d", template.getId().getTagId());
//            String value = getValueForTag(template, qrData, isDynamic);
//
//            if (value == null) {
//                if (!"63".equals(tag) && "1".equals(String.valueOf(template.getRequired()))) {
//                    throw new RuntimeException("Required field with tag " + tag + " is missing.");
//                } else {
//                    continue;
//                }
//            }
//
//            List<QrTlvSubtemplate> subTemplates = qrTlvSubtemplateRepository.findByQrTlvTemplate(template);
//            String nestedSubTemplate = buildNestedTags(subTemplates, qrData);
//            if (!nestedSubTemplate.isEmpty()) {
//                value += nestedSubTemplate;
//            }
//
//            String length = String.format("%02d", value.length());
//            qrDataString.append(tag).append(length).append(value);
//        }
//
//        String crc = CRCUtils.computeCRC(qrDataString.toString());
//        qrDataString.append("63").append("04").append(crc);
//
//        return qrDataString.toString();
//    }
//
//    private String buildNestedTags(List<QrTlvSubtemplate> subTemplates, Map<String, Object> qrData) {
//        if (subTemplates == null || subTemplates.isEmpty()) {
//            return "";
//        }
//
//        StringBuilder nestedString = new StringBuilder();
//        subTemplates.sort((a, b) -> a.getSubTagId().compareTo(b.getSubTagId()));
//
//        for (QrTlvSubtemplate subTemplate : subTemplates) {
//            String tag = String.format("%02d", subTemplate.getSubTagId());
//            String value = Optional.ofNullable(qrData.get(tag)).map(Object::toString).orElse(subTemplate.getContentValue());
//
//            if (value == null) {
//                continue;
//            }
//
//            String length = String.format("%02d", value.length());
//            nestedString.append(tag).append(length).append(value);
//        }
//
//        return nestedString.toString();
//    }
//
//    private static String getValueForTag(QrTlvTemplate template, Map<String, Object> qrData, boolean isDynamic) {
//        return switch (String.valueOf(template.getId().getTagId())) {
//            case "1" -> isDynamic ? "12" : "11";
//            case "59" -> (String) qrData.get("merchantName");
//            case "60" -> (String) qrData.get("merchantCity");
//            case "61" -> (String) qrData.get("postalCode");
//            case "29" -> buildMerchantAccountInfo((String) qrData.get("merchantAccountInformation"));
//            case "52" -> (String) qrData.get("merchantCategoryCode");
//            case "54" -> isDynamic ? formatAmountForTLV(qrData.get("transactionAmount")) : null;
//            case "82" -> "01" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm"));
//            default -> template.getContentValue();
//        };
//    }
//
//    private static String buildMerchantAccountInfo(String merchantAccount) {
//        String qrSubDomain = "ke.go.qr";
//        String keQrSubDomainTag = "00";
//        String keQrSubDomainLength = String.format("%02d", qrSubDomain.length());
//        String keQrSubDomainTLV = keQrSubDomainTag + keQrSubDomainLength + qrSubDomain;
//
//        String pspIdTag = "11";
//        String pspIdLength = String.format("%02d", merchantAccount.length());
//        String pspIdTLV = pspIdTag + pspIdLength + merchantAccount;
//
//        String merchantAccountTag = "29";
//        String merchantAccountValue = keQrSubDomainTLV + pspIdTLV;
//        String merchantAccountLength = String.format("%02d", merchantAccountValue.length());
//
//        return merchantAccountTag + merchantAccountLength + merchantAccountValue;
//    }
//
//    private static String formatAmountForTLV(Object value) {
//        BigDecimal amount = new BigDecimal(value.toString());
//        return String.format("%.2f", amount);
//    }
//}



//package com.coop.qrcodeengine.api.utils;
//
////import com.coop.qrcodeengine.api.entity.DynamicQrCodeTemplate;
////import com.coop.qrcodeengine.api.entity.QrTlvSubtemplate;
//import com.coop.qrcodeengine.api.entity.QrTlvTemplate;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Map;
//
//public class QrCodeBuilder {
//    /**
//     * Builds a QR Code Data String dynamically using the TLV (Tag-Length-Value) format.
//     * Supports both Static and Dynamic QR Code templates.
//     * @param templateList List of QR templates from DB
//     * @param qrData Map containing key-value pairs of QR fields
//     * @param isDynamic Determines if it's a static or dynamic QR
//     * @return QR Code string formatted with TLV
//     */
//    public static String buildQRCodeData(List<QrTlvTemplate> templateList, Map<String, Object> qrData, boolean isDynamic) {
//        StringBuilder qrDataString = new StringBuilder();
//
////        // 1️⃣ Add mandatory fields for CBK compliance
////        qrDataString.append("00").append("02").append("01"); // Payload Format Indicator
////        qrDataString.append("01").append("02").append(isDynamic ? "12" : "11"); // Static (11) or Dynamic (12) QR
//
//        // 2️⃣ Sort template by tag ID to maintain proper structure
//        // TODO: Fix this
////        templateList.sort(Comparator.comparingInt(QrTlvTemplate::getId));
//
//        // 3️⃣ Append template-based and user-provided values
//        for (QrTlvTemplate template : templateList) {
//            String tag = String.format("%02d", template.getId());
//            String value = getValueForTag(template, qrData, isDynamic);
//
//            // 4️⃣ Handle null values based on 'required' flag
//            if (value == null) {
//                if (!"63".equals(tag) && "1".equals(String.valueOf(template.getRequired()))) {
//                    throw new RuntimeException("Required field with tag " + tag + " is missing.");
//                } else {
//                    continue; // Skip optional fields if null
//                }
//            }
//
//            String length = String.format("%02d", value.length());
//            qrDataString.append(tag).append(length).append(value);
//        }
//
//        // 5️⃣ Compute CRC for CBK standards and append at the end
//        String crc = CRCUtils.computeCRC(qrDataString.toString());
//        qrDataString.append("63").append("04").append(crc);
//
//        return qrDataString.toString();
//    }
//
//    /**
//     * Extracts values from qrData hashmap, fallback to DB values
//     * @param template Static/Dynamic QR template field
//     * @param qrData Input data from request
//     * @param isDynamic Indicates if it's a dynamic QR
//     * @return Correct value to use for the field
//     */
//    private static String getValueForTag(QrTlvTemplate template, Map<String, Object> qrData, boolean isDynamic) {
//        return switch (String.valueOf(template.getId())) {
//            case "1" -> isDynamic ? "12" : "11";
//            case "59" -> (String) qrData.get("merchantName"); // Merchant Name
//            case "60" -> (String) qrData.get("merchantCity"); // Merchant City
//            case "61" -> (String) qrData.get("postalCode"); // Postal Code
//            case "29" -> buildMerchantAccountInfo((String) qrData.get("merchantAccountInformation")); // Merchant Account Info
//            case "52" -> (String) qrData.get("merchantCategoryCode"); // Merchant Category Code
//            //case "53" -> qrData.containsKey("transactionCurrency") ? qrData.get("transactionCurrency").toString() : null;
//            case "54" -> isDynamic ? formatAmountForTLV(qrData.get("transactionAmount")) : null;
//            //case "54" -> isDynamic ? formatBigDecimal(convertToBigDecimal(qrData.get("transactionAmount"))) : null;
//            //case "54" -> isDynamic ? qrData.get("transactionAmount").toString() : null; // Amount only for dynamic
//            case "82" -> "01" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm")); // Timestamp
//            default -> template.getContentValue(); // Use DB default value
//        };
//    }
//
//    /**
//     * Constructs Merchant Account Information using CBK standard format
//     * @param merchantAccount Merchant's account identifier
//     * @return Properly formatted Merchant Account Information TLV
//     */
//    private static String buildMerchantAccountInfo(String merchantAccount) {
//        String qrSubDomain = "ke.go.qr"; // Example, should be fetched from config
//
//        // Construct KE QR Sub-Domain TLV
//        String keQrSubDomainTag = "00";
//        String keQrSubDomainLength = String.format("%02d", qrSubDomain.length());
//        String keQrSubDomainTLV = keQrSubDomainTag + keQrSubDomainLength + qrSubDomain;
//
//        // Construct PSP ID TLV
//        String pspIdTag = "11";
//        String pspIdLength = String.format("%02d", merchantAccount.length());
//        String pspIdTLV = pspIdTag + pspIdLength + merchantAccount;
//
//        // Construct Merchant Account Info TLV
//        String merchantAccountTag = "29";
//        String merchantAccountValue = keQrSubDomainTLV + pspIdTLV;
//        String merchantAccountLength = String.format("%02d", merchantAccountValue.length());
//
//        return merchantAccountTag + merchantAccountLength + merchantAccountValue;
//    }
//
//    // ✅ Properly formats BigDecimal for QR Code TLV standard
//    private static String formatBigDecimal(BigDecimal amount) {
//        return amount.stripTrailingZeros().toPlainString();
//    }
//
//    private static BigDecimal convertToBigDecimal(Object value) {
//        if (value == null) {
//            return BigDecimal.ZERO; // Default to 0 if null
//        } else if (value instanceof BigDecimal) {
//            return (BigDecimal) value; // Already BigDecimal
//        } else if (value instanceof Double) {
//            return BigDecimal.valueOf((Double) value); // Convert Double → BigDecimal
//        } else if (value instanceof String) {
//            return new BigDecimal((String) value); // Convert String → BigDecimal
//        } else {
//            throw new IllegalArgumentException("Invalid type for transactionAmount: " + value.getClass().getName());
//        }
//    }
//
//    private static String formatAmountForTLV(Object value) {
//        BigDecimal amount = convertToBigDecimal(value);
//        return String.format("%.2f", amount); // 1000.00
//    }
//}
