package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.config.QrConfigProperties;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeResponse;
import com.coop.qrcodeengine.api.entity.QrCodeDetails;
import com.coop.qrcodeengine.api.entity.DynamicQrCodeTemplate;
import com.coop.qrcodeengine.api.exception.QrCodeGenerationException;
import com.coop.qrcodeengine.api.repository.QrCodeDetailsRepository;
import com.coop.qrcodeengine.api.repository.DynamicQrCodeTemplateRepository;
import com.coop.qrcodeengine.api.utils.QrCodeBuilder;
import com.coop.qrcodeengine.api.utils.QrCodeGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QrCodeServiceImpl implements QrCodeService {
    private static final Logger LOGGER = LogManager.getLogger(QrCodeServiceImpl.class);

    private final DynamicQrCodeTemplateRepository qrCodeTemplateRepository;
    private final QrCodeDetailsRepository qrCodeDetailsRepository;
    private final QrCodeLoggingService qrCodeLoggingService;
    private final QrConfigProperties qrConfig;

    public QrCodeServiceImpl(DynamicQrCodeTemplateRepository qrCodeTemplateRepository,
                             QrCodeDetailsRepository qrCodeDetailsRepository,
                             QrCodeLoggingService qrCodeLoggingService,
                             QrConfigProperties qrConfig) {
        this.qrCodeTemplateRepository = qrCodeTemplateRepository;
        this.qrCodeDetailsRepository = qrCodeDetailsRepository;
        this.qrCodeLoggingService = qrCodeLoggingService;
        this.qrConfig = qrConfig;
    }

    /**
     * Generate Static QR Code
     */
    public GenerateQRCodeResponse generateStaticQrCode(GenerateQRCodeRequest qrCodeRequest) {
        return generateQrCode(qrCodeRequest, false);
    }

    /**
     * Generate Dynamic QR Code
     */
    public GenerateQRCodeResponse generateDynamicQrCode(GenerateQRCodeRequest qrCodeRequest) {
        return generateQrCode(qrCodeRequest, true);
    }

    /**
     * Common method for generating Static & Dynamic QR Codes
     */
    private GenerateQRCodeResponse generateQrCode(GenerateQRCodeRequest qrCodeRequest, boolean isDynamic) {
        String requestId = UUID.randomUUID().toString();
        qrCodeLoggingService.logRequest(requestId, qrCodeRequest, isDynamic ? "GenerateDynamicQrCode" : "GenerateStaticQrCode");

        try {
            // Fetch QR template from the database
            List<DynamicQrCodeTemplate> qrTemplate = qrCodeTemplateRepository.findAll();
            if (qrTemplate.isEmpty()) {
                throw new RuntimeException("No QR Code template found in database");
            }

            if (isDynamic) {
                validateDynamicQrFields(qrCodeRequest.getQrData());
            }

            LOGGER.info("Generating {} QR Code for {}", isDynamic ? "Dynamic" : "Static",
                    qrCodeRequest.getQrData().get("merchantName"));

            // Build QR Code Data String using QrCodeBuilder
            String qrData = QrCodeBuilder.buildQRCodeData(qrTemplate, qrCodeRequest.getQrData(), isDynamic);

            // Generate QR Code Image
            byte[] qrImage = QrCodeGenerator.generateStyledQRCode(qrData, "PNG");
            String base64Qr = java.util.Base64.getEncoder().encodeToString(qrImage);

            // Save QR Details
            saveQrCodeDetails(qrCodeRequest, qrData, base64Qr, isDynamic);

            qrCodeLoggingService.updateLog(requestId, qrData);
            return new GenerateQRCodeResponse(base64Qr, "PNG", 512);
        } catch (Exception e) {
            LOGGER.error("Error generating QR Code", e);
            qrCodeLoggingService.updateLog(requestId, "ERROR: " + e.getMessage());
            throw new QrCodeGenerationException("Failed to generate QR Code");
        }
    }

    /**
     * Saves QR Code Details into DB
     */
    private void saveQrCodeDetails(GenerateQRCodeRequest request, String qrData, String base64QrImage, boolean isDynamic) {
        QrCodeDetails qrCodeDetails = QrCodeDetails.builder()
                .qrCodeId(UUID.randomUUID().toString())
                .qrCodeType(isDynamic ? "DYNAMIC" : "STATIC")
                .qrCodeVersion("01")
                .merchantsName((String) request.getQrData().get("merchantName"))
                .merchantCity((String) request.getQrData().get("merchantCity"))
                .postalCode((String) request.getQrData().get("postalCode"))
                .transactionCurrency((String) request.getQrData().get("transactionCurrency"))
                .transactionAmount(request.getQrData().get("transactionAmount") != null ?
                        (BigDecimal) (request.getQrData().get("transactionAmount")) : null)
                .qrCodeData(qrData)
                .qrCodeImage(base64QrImage)
                .creationDate(Date.from(Instant.now()))
                .build();

        qrCodeDetailsRepository.save(qrCodeDetails);
    }

    private void validateDynamicQrFields(Map<String, Object> qrData) {
        List<String> requiredFields = List.of("merchantName", "transactionAmount");
        for (String field : requiredFields) {
            if (!qrData.containsKey(field) || qrData.get(field) == null) {
                throw new IllegalArgumentException(field + " is required for Dynamic QR");
            }
        }
    }
}


//package com.coop.qrcodeengine.api.service;
//
//import com.coop.qrcodeengine.api.config.QrConfigProperties;
//import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
//import com.coop.qrcodeengine.api.dto.GenerateQRCodeResponse;
//import com.coop.qrcodeengine.api.entity.QrCodeDetails;
//import com.coop.qrcodeengine.api.entity.StaticQrCodeTemplate;
//import com.coop.qrcodeengine.api.exception.QrCodeGenerationException;
//import com.coop.qrcodeengine.api.repository.QrCodeDetailsRepository;
//import com.coop.qrcodeengine.api.repository.StaticQrCodeTemplateRepository;
//import com.coop.qrcodeengine.api.utils.CRCUtils;
//import com.coop.qrcodeengine.api.utils.QrCodeBuilder;
//import com.coop.qrcodeengine.api.utils.QrCodeGenerator;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Service
//public class QrCodeServiceImpl implements QrCodeService {
//    private static final Logger LOGGER = LogManager.getLogger(QrCodeServiceImpl.class);
//
//    private final StaticQrCodeTemplateRepository staticQrCodeTemplateRepository;
//    private final QrCodeDetailsRepository qrCodeDetailsRepository;
//    private final QrCodeLoggingService qrCodeLoggingService;
//
//    private final QrConfigProperties qrConfig;
//
//    public QrCodeServiceImpl(StaticQrCodeTemplateRepository staticQrCodeTemplateRepository, QrCodeDetailsRepository qrCodeDetailsRepository, QrCodeLoggingService qrCodeLoggingService, QrConfigProperties qrConfig) {
//        this.staticQrCodeTemplateRepository = staticQrCodeTemplateRepository;
//        this.qrCodeDetailsRepository = qrCodeDetailsRepository;
//        this.qrCodeLoggingService = qrCodeLoggingService;
//        this.qrConfig = qrConfig;
//    }
//
//    public GenerateQRCodeResponse generateStaticQrCode(GenerateQRCodeRequest qrCodeRequest) {
//        String requestId = UUID.randomUUID().toString(); // Generate unique request ID
//
//        // 1️⃣ Log the request at the start (PENDING)
//        qrCodeLoggingService.logRequest(requestId, qrCodeRequest, "GenerateStaticQrCode");
//
//        try {
//            LOGGER.info("Fetching static QR code template from database...");
//
//            // 2️⃣ Fetch static QR template from DB
//            List<StaticQrCodeTemplate> qrTemplate = staticQrCodeTemplateRepository.findAll();
//            if (qrTemplate.isEmpty()) {
//                throw new RuntimeException("No static QR Code template found in database");
//            }
//            LOGGER.info("Generating static QR code for {}", qrCodeRequest.getQrData().get("MerchantName"));
//
//            // 3️⃣ Construct QR Code Data
//            String qrData = buildQRCodeData(qrTemplate, qrCodeRequest);
//
//            // 4️⃣ Generate QR Code Image
//            String format = "PNG";
//            int size = 512;
//            byte[] qrImage = QrCodeGenerator.generateStyledQRCode(qrData, format);
//            String base64Qr = Base64.getEncoder().encodeToString(qrImage);
//
//            // 5️⃣ Save QR Code Details
//            saveQrCodeDetails(qrTemplate, qrCodeRequest, qrData, base64Qr);
//
//            // 5️⃣ Log Request
////            logQrCodeRequest(qrCodeRequest, qrData);
//
//            // 6️⃣ Update log after QR is generated (SUCCESS)
//            qrCodeLoggingService.updateLog(requestId, qrData);
//
//            return new GenerateQRCodeResponse(base64Qr, format, size);
//        }
//        catch (Exception e) {
//            LOGGER.error("Error generating QR Code", e);
//
//            // 7️⃣ Update log with FAILURE if an error occurs
//            qrCodeLoggingService.updateLog(requestId, "ERROR: " + e.getMessage());
//
//            throw new QrCodeGenerationException("Failed to generate QR Code");
//        }
//    }
//
//    public GenerateQRCodeResponse generateDynamicQrCode(GenerateQRCodeRequest qrCodeRequest) {
//        String requestId = UUID.randomUUID().toString();
//        qrCodeLoggingService.logRequest(requestId, qrCodeRequest, "GenerateDynamicQrCode");
//
//        try {
//            // 1️⃣ Validate mandatory fields
//            validateDynamicQrFields(qrCodeRequest.getQrData());
//
//            // 2️⃣ Build QR Code String using new QRCodeBuilder
//            String qrData = QrCodeBuilder.buildQRCodeData(qrCodeRequest.getQrData(), true);
//
//            // 3️⃣ Generate QR Code Image
//            byte[] qrImage = QrCodeGenerator.generateStyledQRCode(qrData, "PNG");
//            String base64Qr = Base64.getEncoder().encodeToString(qrImage);
//
//            // 4️⃣ Save details in DB
//            saveQrCodeDetails(qrCodeRequest.getQrData(), qrData, base64Qr);
//
//            return new GenerateQRCodeResponse(base64Qr, "PNG", 512);
//        } catch (Exception e) {
//            qrCodeLoggingService.updateLog(requestId, "ERROR: " + e.getMessage());
//            throw new QrCodeGenerationException("Failed to generate Dynamic QR Code");
//        }
//    }
//
//
//    private String buildQRCodeData(List<StaticQrCodeTemplate> templateList, GenerateQRCodeRequest request) {
//        StringBuilder qrData = new StringBuilder();
//
//        // 1️⃣ Sort the template list by tagId (ascending order)
//        templateList.sort(Comparator.comparingInt(StaticQrCodeTemplate::getId));
//
//        for (StaticQrCodeTemplate template : templateList) {
//            String tag = String.format("%02d", template.getId()); // Ensure tag is always 2 digits
//            String value = getValueForTag(template, request);
//
//            // 2️⃣ Handle null values based on 'required' flag
//            if (value == null) {
//                if (!"63".equals(tag) && "1".equals(String.valueOf(template.getRequired()))) // Required but null, except for tag 63
//                {
//                    throw new RuntimeException("Required field with tag " + tag + " is missing from input or database.");
//                } else {
//                    continue; // Skip optional fields if null
//                }
//            }
//
//            String length = String.format("%02d", value.length());
//            qrData.append(tag).append(length).append(value);
//        }
//
//        // 3️⃣ Compute CRC and append at the end
//        String crcValue = CRCUtils.computeCRC(qrData.toString());
//        qrData.append("63").append("04").append(crcValue);
//
//        return qrData.toString();
//
////        return constructTLVString(templateList, request);
//    }
//
//    private String getValueForTag(StaticQrCodeTemplate template, GenerateQRCodeRequest request) {
//        return switch (String.valueOf(template.getId())) {
//            case "59" -> request.getMerchantName(); // Merchant Name
//            case "60" -> request.getMerchantCity(); // Merchant City
//            case "61" -> request.getPostalCode(); // Postal Code
//            case "29" -> buildMerchantAccountInfo(request.getMerchantAccountInformation()); // Merchant Account Info
//            case "52" -> request.getMerchantCategoryCode(); // Merchant Category Code
//            case "82" -> "01" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm")); // Timestamp
//            default -> template.getContentValue(); // Use DB default value
//        };
//    }
//
//    private String buildMerchantAccountInfo(String merchantAccount) {
//        // Fetch constants from config
//        String qrSubDomain = qrConfig.getKeQrSubDomain(); // ke.go.qr
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
//    private void saveQrCodeDetails(List<StaticQrCodeTemplate> qrTemplate, GenerateQRCodeRequest request, String qrData, String base64QrImage) {
//        QrCodeDetails qrCodeDetails = QrCodeDetails.builder()
//                .qrCodeId(UUID.randomUUID().toString()) // ✅ Generate unique ID
//                .qrCodeType("STATIC")
//                .qrCodeVersion("01")
//                .merchantsName(request.getQrData().get("MerchantName"))
//                .countryCode(qrTemplate.stream()
//                        .filter(template -> template.getId() == 58)
//                        .map(StaticQrCodeTemplate::getContentValue)
//                        .findFirst()
//                        .orElse(null))
//                .merchantCity(request.getMerchantCity())
//                .postalCode(request.getPostalCode())
//                .merchantAccountInformation(request.getMerchantAccountInformation())
//                .merchantCategoryCode(request.getMerchantCategoryCode())
//                .transactionCurrency("KES")
//                .qrCodeData(qrData)
//                .qrCodeImage(base64QrImage)
//                .checkSumValue(qrData.substring(qrData.length() - 4))
//                .additionalInformation("00")
//                .messageId("null")
//                .creationDate(Date.from(Instant.now()))
//                .build();
//
//        qrCodeDetailsRepository.save(qrCodeDetails);
//    }
//
//    private void validateDynamicQrFields(Map<String, Object> qrData) {
//        List<String> requiredFields = List.of("merchantName", "transactionCurrency", "transactionAmount");
//
//        for (String field : requiredFields) {
//            if (!qrData.containsKey(field) || qrData.get(field) == null) {
//                throw new IllegalArgumentException(field + " is required for Dynamic QR");
//            }
//        }
//    }
//
//
//}
