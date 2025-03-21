package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeResponse;
import com.coop.qrcodeengine.api.entity.*;
import com.coop.qrcodeengine.api.exception.QrCodeGenerationException;
import com.coop.qrcodeengine.api.repository.*;
import com.coop.qrcodeengine.api.utils.QrCodeBuilder;
import com.coop.qrcodeengine.api.utils.QrImageGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QrCodeServiceImpl implements QrCodeService {
    private static final Logger LOGGER = LogManager.getLogger(QrCodeServiceImpl.class);

    private final QrCodeLoggingService qrCodeLoggingService;
    private final QrTlvTemplateRepository qrTlvTemplateRepository;
    private final QrCodeBuilder qrCodeBuilder;
    private final QrLogoTemplateRepository qrLogoTemplateRepository;
    private final QrCodeStorageRepository qrCodeStorageRepository;
    private final QrCodeDetailsRepository qrCodeDetailsRepository;

    public QrCodeServiceImpl(QrCodeLoggingService qrCodeLoggingService,
                             QrTlvTemplateRepository qrTlvTemplateRepository,
                             QrCodeBuilder qrCodeBuilder,
                             QrLogoTemplateRepository qrLogoTemplateRepository, QrCodeStorageRepository qrCodeStorageRepository, QrCodeDetailsRepository qrCodeDetailsRepository) {
        this.qrCodeLoggingService = qrCodeLoggingService;
        this.qrTlvTemplateRepository = qrTlvTemplateRepository;
        this.qrCodeBuilder = qrCodeBuilder;
        this.qrLogoTemplateRepository = qrLogoTemplateRepository;
        this.qrCodeStorageRepository = qrCodeStorageRepository;
        this.qrCodeDetailsRepository = qrCodeDetailsRepository;
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

            // Fetch logo image from DB
            byte[] logoImage = fetchLogoImage(qrCodeRequest.getChannelId());

            // Generate QR Code Image with Logo
            byte[] qrImage = QrImageGenerator.generateStyledQRCode(qrData, "PNG", logoImage);
            String base64Qr = Base64.getEncoder().encodeToString(qrImage);

            // Save QR Details
            saveQrCodeDetails(qrCodeRequest, qrData, base64Qr, isDynamic);

            // Create Response Object
            GenerateQRCodeResponse response = new GenerateQRCodeResponse(base64Qr, "PNG", 400);

            // ✅ Log the successful response
            qrCodeLoggingService.updateLog(requestId, response, "SUCCESS", "QR Code generated successfully");

            return response;
        } catch (Exception e) {
            LOGGER.error("Error generating QR Code", e);
            // ❌ Failed Response
            GenerateQRCodeResponse errorResponse = new GenerateQRCodeResponse(null, "PNG", 500);

            // ❌ Log the failure in DB
            qrCodeLoggingService.updateLog(requestId, errorResponse, "FAILED", "QR Code generation failed: " + e.getMessage());
            throw new QrCodeGenerationException("Failed to generate QR Code");
        }
    }

    private byte[] fetchLogoImage(Long channelId) {
        return qrLogoTemplateRepository.findByChannelId(channelId)
                .map(QrLogoTemplate::getTemplateImage)
                .orElseThrow(() -> new RuntimeException("No logo found for Channel ID: " + channelId));
    }

    private void saveQrCodeDetails(GenerateQRCodeRequest request, String qrData, String base64QrImage, boolean isDynamic) {
        LOGGER.info("QR Code saved: {}", qrData);

        // ✅ 1️⃣ Save Main QR Code Storage
        QrCodeStorage qrCodeStorage = new QrCodeStorage();
        qrCodeStorage.setQrCodeId(UUID.randomUUID().toString()); // Generate Unique ID
        qrCodeStorage.setQrCodeString(qrData);
        qrCodeStorage.setQrCodeImage(Base64.getDecoder().decode(base64QrImage)); // Convert Base64 to Binary
        qrCodeStorage.setChannelId(request.getChannelId());
        qrCodeStorage.setIsValid('1');
        qrCodeStorage.setStatus("ACTIVE");
        qrCodeStorage.setChecksumValue(qrData.substring(qrData.length() - 4));
        qrCodeStorage.setCreatedAt(Date.from(Instant.now()));
        qrCodeStorageRepository.save(qrCodeStorage);

        // TODO: Fully implement saving logic for qr code details, might be easier after read and verify API
        // ✅ 3️⃣ Batch Insert QR Code Details (Avoid Per-Loop Inserts)
        List<QrCodeDetails> qrDetailsList = request.getQrData().entrySet().stream()
                .map(entry -> {
                    QrCodeDetailsId qrCodeDetailsId = new QrCodeDetailsId(qrCodeStorage.getQrCodeId(), entry.getKey());

                    QrCodeDetails detail = new QrCodeDetails();
                    detail.setId(qrCodeDetailsId); // Set Composite Key
                    detail.setQrCodeType(isDynamic ? "DynamicQR" : "StaticQR");
                    detail.setChannelId(request.getChannelId());
                    detail.setFieldValue(entry.getValue().toString()); // Field Value
                    detail.setCreatedAt(Date.from(Instant.now()));

                    return detail;
                })
                .collect(Collectors.toList());

        // Perform a **batch insert** instead of multiple DB calls
        qrCodeDetailsRepository.saveAll(qrDetailsList);

        LOGGER.info("QR Code Details saved successfully.");
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