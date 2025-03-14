package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.config.QrConfigProperties;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeResponse;
import com.coop.qrcodeengine.api.entity.QrTlvTemplate;
import com.coop.qrcodeengine.api.exception.QrCodeGenerationException;
import com.coop.qrcodeengine.api.repository.QrTlvTemplateRepository;
import com.coop.qrcodeengine.api.utils.QrCodeBuilder;
import com.coop.qrcodeengine.api.utils.QrCodeGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QrCodeServiceImpl implements QrCodeService {
    private static final Logger LOGGER = LogManager.getLogger(QrCodeServiceImpl.class);

    private final QrCodeLoggingService qrCodeLoggingService;
    private final QrConfigProperties qrConfig;
    private final QrTlvTemplateRepository qrTlvTemplateRepository;
    private final QrCodeBuilder qrCodeBuilder;

    public QrCodeServiceImpl(QrCodeLoggingService qrCodeLoggingService,
                             QrConfigProperties qrConfig,
                             QrTlvTemplateRepository qrTlvTemplateRepository,
                             QrCodeBuilder qrCodeBuilder) {
        this.qrCodeLoggingService = qrCodeLoggingService;
        this.qrConfig = qrConfig;
        this.qrTlvTemplateRepository = qrTlvTemplateRepository;
        this.qrCodeBuilder = qrCodeBuilder;
    }

    @Override
    public GenerateQRCodeResponse generateStaticQrCode(GenerateQRCodeRequest qrCodeRequest) {
        return generateQrCode(qrCodeRequest, false);
    }

    @Override
    public GenerateQRCodeResponse generateDynamicQrCode(GenerateQRCodeRequest qrCodeRequest) {
        return generateQrCode(qrCodeRequest, true);
    }

    private GenerateQRCodeResponse generateQrCode(GenerateQRCodeRequest qrCodeRequest, boolean isDynamic) {
        String requestId = UUID.randomUUID().toString();
        qrCodeLoggingService.logRequest(requestId, qrCodeRequest, isDynamic ? "GenerateDynamicQrCode" : "GenerateStaticQrCode");

        try {
            // Fetch TLV templates for the channel and template type
            // Fetch TLV templates based on request type
            List<QrTlvTemplate> qrTemplates = isDynamic
                    ? qrTlvTemplateRepository.findByIsDynamic('1')
                    : qrTlvTemplateRepository.findByIsStatic('1');
            if (qrTemplates.isEmpty()) {
                throw new RuntimeException("No QR Code template found in database");
            }

            if (isDynamic) {
                validateDynamicQrFields(qrCodeRequest.getQrData());
            }

            LOGGER.info("Generating {} QR Code for {}", isDynamic ? "Dynamic" : "Static", qrCodeRequest.getQrData().get("merchantName"));

            // Build QR Code Data String using QrCodeBuilder
            String qrData = qrCodeBuilder.buildQRCodeData(qrTemplates, qrCodeRequest.getQrData(), isDynamic);

            // Generate QR Code Image
            byte[] qrImage = QrCodeGenerator.generateStyledQRCode(qrData, "PNG");
            String base64Qr = Base64.getEncoder().encodeToString(qrImage);

            // Save QR Details
            saveQrCodeDetails(qrCodeRequest, qrData, base64Qr, isDynamic);

            qrCodeLoggingService.updateLog(requestId, qrData);
            return new GenerateQRCodeResponse(base64Qr, "PNG", 400);
        } catch (Exception e) {
            LOGGER.error("Error generating QR Code", e);
            qrCodeLoggingService.updateLog(requestId, "ERROR: " + e.getMessage());
            throw new QrCodeGenerationException("Failed to generate QR Code");
        }
    }

    private void saveQrCodeDetails(GenerateQRCodeRequest request, String qrData, String base64QrImage, boolean isDynamic) {
        // TODO: Implement saving logic if required in the new structure
        LOGGER.info("QR Code saved: {}", qrData);
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
////import com.coop.qrcodeengine.api.entity.QrCodeDetails;
////import com.coop.qrcodeengine.api.entity.DynamicQrCodeTemplate;
//import com.coop.qrcodeengine.api.entity.QrTlvTemplate;
//import com.coop.qrcodeengine.api.exception.QrCodeGenerationException;
////import com.coop.qrcodeengine.api.repository.QrCodeDetailsRepository;
////import com.coop.qrcodeengine.api.repository.DynamicQrCodeTemplateRepository;
//import com.coop.qrcodeengine.api.repository.QrTlvTemplateRepository;
//import com.coop.qrcodeengine.api.utils.HighQualityQrGenerator;
//import com.coop.qrcodeengine.api.utils.QrCodeBuilder;
//import com.coop.qrcodeengine.api.utils.QrCodeGenerator;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//public class QrCodeServiceImpl implements QrCodeService {
//    private static final Logger LOGGER = LogManager.getLogger(QrCodeServiceImpl.class);
//
////    private final DynamicQrCodeTemplateRepository qrCodeTemplateRepository;
////    private final QrCodeDetailsRepository qrCodeDetailsRepository;
//    private final QrCodeLoggingService qrCodeLoggingService;
//    private final QrConfigProperties qrConfig;
//    private final QrTlvTemplateRepository qrTlvTemplateRepository;
//
//    public QrCodeServiceImpl(QrCodeLoggingService qrCodeLoggingService,
//                             QrConfigProperties qrConfig, QrTlvTemplateRepository qrTlvTemplateRepository) {
//        this.qrCodeLoggingService = qrCodeLoggingService;
//        this.qrConfig = qrConfig;
//        this.qrTlvTemplateRepository = qrTlvTemplateRepository;
//    }
//
//    /**
//     * Generate Static QR Code
//     */
//    public GenerateQRCodeResponse generateStaticQrCode(GenerateQRCodeRequest qrCodeRequest) {
//        return generateQrCode(qrCodeRequest, false);
//    }
//
//    /**
//     * Generate Dynamic QR Code
//     */
//    public GenerateQRCodeResponse generateDynamicQrCode(GenerateQRCodeRequest qrCodeRequest) {
//        return generateQrCode(qrCodeRequest, true);
//    }
//
//    /**
//     * Common method for generating Static & Dynamic QR Codes
//     */
//    private GenerateQRCodeResponse generateQrCode(GenerateQRCodeRequest qrCodeRequest, boolean isDynamic) {
//        String requestId = UUID.randomUUID().toString();
//        qrCodeLoggingService.logRequest(requestId, qrCodeRequest, isDynamic ? "GenerateDynamicQrCode" : "GenerateStaticQrCode");
//
//        try {
//            // Fetch QR template from the database
//            List<QrTlvTemplate> qrTemplate = qrTlvTemplateRepository.findAll();
////            List<DynamicQrCodeTemplate> qrTemplate = qrCodeTemplateRepository.findAll();
//            if (qrTemplate.isEmpty()) {
//                throw new RuntimeException("No QR Code template found in database");
//            }
//
//            if (isDynamic) {
//                validateDynamicQrFields(qrCodeRequest.getQrData());
//            }
//
//            LOGGER.info("Generating {} QR Code for {}", isDynamic ? "Dynamic" : "Static",
//                    qrCodeRequest.getQrData().get("merchantName"));
//
//            // Build QR Code Data String using QrCodeBuilder
//            String qrData = QrCodeBuilder.buildQRCodeData(qrTemplate, qrCodeRequest.getQrData(), isDynamic);
//
//            // Generate QR Code Image
//            byte[] qrImage = QrCodeGenerator.generateStyledQRCode(qrData, "PNG");
////            byte[] qrImage = HighQualityQrGenerator.generateQrCodeWithLogo(qrData);
//            String base64Qr = java.util.Base64.getEncoder().encodeToString(qrImage);
//
//            // Save QR Details
//            saveQrCodeDetails(qrCodeRequest, qrData, base64Qr, isDynamic);
//
//            qrCodeLoggingService.updateLog(requestId, qrData);
//            return new GenerateQRCodeResponse(base64Qr, "PNG", 512);
//        } catch (Exception e) {
//            LOGGER.error("Error generating QR Code", e);
//            qrCodeLoggingService.updateLog(requestId, "ERROR: " + e.getMessage());
//            throw new QrCodeGenerationException("Failed to generate QR Code");
//        }
//    }
//
//    /**
//     * Saves QR Code Details into DB
//     */
//    private void saveQrCodeDetails(GenerateQRCodeRequest request, String qrData, String base64QrImage, boolean isDynamic) {
//        // TODO: Fix this
////        QrCodeDetails qrCodeDetails = QrCodeDetails.builder()
////                .qrCodeId(UUID.randomUUID().toString())
////                .qrCodeType(isDynamic ? "DYNAMIC" : "STATIC")
////                .qrCodeVersion("01")
////                .merchantsName((String) request.getQrData().get("merchantName"))
////                .merchantCity((String) request.getQrData().get("merchantCity"))
////                .postalCode((String) request.getQrData().get("postalCode"))
////                .transactionCurrency((String) request.getQrData().get("transactionCurrency"))
////                .transactionAmount(request.getQrData().get("transactionAmount") != null ?
////                        (BigDecimal) (request.getQrData().get("transactionAmount")) : null)
////                .qrCodeData(qrData)
////                .qrCodeImage(base64QrImage)
////                .creationDate(Date.from(Instant.now()))
////                .build();
////
////        qrCodeDetailsRepository.save(qrCodeDetails);
//    }
//
//    private void validateDynamicQrFields(Map<String, Object> qrData) {
//        List<String> requiredFields = List.of("merchantName", "transactionAmount");
//        for (String field : requiredFields) {
//            if (!qrData.containsKey(field) || qrData.get(field) == null) {
//                throw new IllegalArgumentException(field + " is required for Dynamic QR");
//            }
//        }
//    }
//}
