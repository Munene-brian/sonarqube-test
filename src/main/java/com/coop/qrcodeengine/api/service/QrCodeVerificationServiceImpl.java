package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.dto.VerifyQRCodeRequest;
import com.coop.qrcodeengine.api.dto.VerifyQRCodeResponse;
import com.coop.qrcodeengine.api.entity.QrCodeStorage;
import com.coop.qrcodeengine.api.entity.QrTlvSubtemplate;
import com.coop.qrcodeengine.api.entity.QrTlvTemplate;
import com.coop.qrcodeengine.api.repository.QrCodeStorageRepository;
import com.coop.qrcodeengine.api.repository.QrFeatureTypeRepository;
import com.coop.qrcodeengine.api.repository.QrTlvSubtemplateRepository;
import com.coop.qrcodeengine.api.repository.QrTlvTemplateRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QrCodeVerificationServiceImpl implements QrCodeVerificationService {
    private static final Logger LOGGER = LogManager.getLogger(QrCodeVerificationServiceImpl.class);
    private final QrTlvTemplateRepository qrTlvTemplateRepository;
    private final QrTlvSubtemplateRepository qrTlvSubtemplateRepository;
    private final QrFeatureTypeRepository qrFeatureTypeRepository;
    private final QrCodeStorageRepository qrCodeStorageRepository;

    public QrCodeVerificationServiceImpl(QrTlvTemplateRepository qrTlvTemplateRepository, QrTlvSubtemplateRepository qrTlvSubtemplateRepository, QrFeatureTypeRepository qrFeatureTypeRepository, QrCodeStorageRepository qrCodeStorageRepository) {
        this.qrTlvTemplateRepository = qrTlvTemplateRepository;
        this.qrTlvSubtemplateRepository = qrTlvSubtemplateRepository;
        this.qrFeatureTypeRepository = qrFeatureTypeRepository;
        this.qrCodeStorageRepository = qrCodeStorageRepository;
    }
//    private final QrCodeRepository qrCodeRepository; // If you need to verify merchants/accounts

    public VerifyQRCodeResponse verifyQrCode(VerifyQRCodeRequest request) {
        LOGGER.info("Verifying QR Code for request: {}", request.getRequestMessageId());

        // Step 1️⃣: Parse TLV Data
        Map<Integer, Object> parsedQrData = parseTlvData(request.getQrCodeData());

        // Step 2️⃣: Validate Checksum
        if (!verifyChecksum(request.getQrCodeData())) {
            return buildErrorResponse(request);
        }

        // Step 3️⃣: Fetch Template Configurations from DB
        Integer channelId = Integer.valueOf(request.getChannelId());
        Map<Integer, QrTlvTemplate> qrTemplate = fetchQrTemplate(channelId, parsedQrData);

        // Step 4️⃣: Extract Dynamic Payment Routing Data
        Map<String, String> paymentRouting = extractPaymentRouting(parsedQrData, qrTemplate);

        // Step 5️⃣: Build Success Response
        return VerifyQRCodeResponse.builder()
                .requestMessageId(request.getRequestMessageId())
                .requestDateTime(Instant.now())
                .responseCode("200")
                .responseDescription("QR Code successfully verified")
                .validationStatus("Valid")
                .paymentRouting(paymentRouting) //TODO: Fix when working
                .build();
    }

    private Map<Integer, QrTlvTemplate> fetchQrTemplate(Integer channelId, Map<Integer, Object> parsedQrData) {
//        Integer templateId = detectTemplateId(parsedQrData);

        // Determine QR Type from Tag 1
        String qrType = (String) parsedQrData.get(1); // Assume Tag 1 holds the QR Type
        String isStatic = (qrType != null && qrType.equals("11")) ? "1" : "0";  // Static QR
        String isDynamic = (qrType != null && qrType.equals("12")) ? "1" : "0"; // Dynamic QR

        List<QrTlvTemplate> templateList = qrTlvTemplateRepository.findByChannelIdAndType(
                channelId, isStatic, isDynamic
        );

        return templateList.stream()
                .collect(Collectors.toMap(t -> t.getId().getTagId(), Function.identity()));
    }


//    private Map<Integer, QrTlvTemplate> fetchQrTemplate(Integer channelId, Map<Integer, String> parsedQrData) {
////        Integer templateId = detectTemplateId(parsedQrData); //TODO: Find out how to get template ID
//
//        List<QrTlvTemplate> templateList = qrTlvTemplateRepository.findByChannelId(channelId);
////        List<QrTlvTemplate> templateList = qrTlvTemplateRepository.findByTemplateIdAndChannelId(templateId, channelId);
//
//        return templateList.stream()
//                .collect(Collectors.toMap(t -> t.getId().getTagId(), Function.identity()));
//    }


private Map<String, String> extractPaymentRouting(Map<Integer, Object> parsedQrData, Map<Integer, QrTlvTemplate> qrTemplate) {
    Map<String, String> paymentDetails = new HashMap<>();

    for (Map.Entry<Integer, QrTlvTemplate> entry : qrTemplate.entrySet()) {
        Integer tagId = entry.getKey();
        QrTlvTemplate template = entry.getValue();

        Object value = parsedQrData.get(tagId);

        if (template.getHasChild() == '0' && template.getVerifyJson() == '1' && value instanceof String) {
            paymentDetails.put(template.getJsonKey(), (String) value);
        } else if (template.getHasChild() == '1' && value instanceof Map) {
            extractSubTemplateData(paymentDetails, (Map<Integer, Object>) value, template.getId().getTagId(), template.getId().getTemplateId(), template.getId().getChannelId());
        }
    }

    // Check & Fetch `journeyType` Name Only Once
    if (paymentDetails.containsKey("journeyType")) {
        String journeyCode = paymentDetails.get("journeyType");
        String journeyName = fetchJourneyTypeName(journeyCode);
        if (journeyName != null) {
            paymentDetails.put("journeyType", journeyName);
        }
    }

    return paymentDetails;
}

    private void extractSubTemplateData(Map<String, String> paymentDetails, Map<Integer, Object> nestedData, Integer parentTagId, Integer templateId, Integer channelId) {
//        List<QrTlvSubtemplate> subTemplates = qrTlvSubtemplateRepository.findByTemplateIdAndChannelId(templateId, channelId);
        List<QrTlvSubtemplate> subTemplates = qrTlvSubtemplateRepository.findByParentTemplateTagId(parentTagId, templateId, channelId);

        for (QrTlvSubtemplate subTemplate : subTemplates) {
            Object value = nestedData.get(subTemplate.getSubTagId());

            if (subTemplate.getHasChild() == '0' && subTemplate.getVerifyJson() == '1' && value instanceof String) {
                paymentDetails.put(subTemplate.getJsonKey(), (String) value);
            } else if (subTemplate.getHasChild() == '1' && value instanceof Map) {
                extractNestedSubTemplateData(paymentDetails, (Map<Integer, Object>) value, subTemplate.getId(), templateId, channelId);
            }
        }
    }

    private void extractNestedSubTemplateData(Map<String, String> paymentDetails, Map<Integer, Object> nestedData, Integer parentSubTagId, Integer templateId, Integer channelId) {
        List<QrTlvSubtemplate> nestedSubTemplates = qrTlvSubtemplateRepository.findByParentSubTagId(parentSubTagId, templateId, channelId);

        for (QrTlvSubtemplate nestedSubTemplate : nestedSubTemplates) {
            Object value = nestedData.get(nestedSubTemplate.getSubTagId());

            if (nestedSubTemplate.getVerifyJson() == '1' && value instanceof String) {
                paymentDetails.put(nestedSubTemplate.getJsonKey(), (String) value);
            } else if (nestedSubTemplate.getHasChild() == '1' && value instanceof Map) {
                extractNestedSubTemplateData(paymentDetails, (Map<Integer, Object>) value, nestedSubTemplate.getSubTagId(), templateId, channelId);
            }
        }
    }

    // Helper Method to Parse TLV Data
    private Map<Integer, Object> parseTlvData(String qrCodeData) {
        Map<Integer, Object> tlvMap = new HashMap<>();
        parseTlvRecursive(qrCodeData, qrCodeData.length(), tlvMap);
        return tlvMap;
    }

    private void parseTlvRecursive(String data, int endIndex, Map<Integer, Object> tlvMap) {
        int index = 0;

        while (index < endIndex) {
            try {
                int tag = Integer.parseInt(data.substring(index, index + 2));
                int length = Integer.parseInt(data.substring(index + 2, index + 4));
                String value = data.substring(index + 4, index + 4 + length);

                // Move index to next tag position
                index += 4 + length;

                // Check if the tag has children based on database metadata
                boolean hasChild = qrTlvTemplateRepository.hasChild(tag) || qrTlvSubtemplateRepository.hasChild(tag);

                if (hasChild) {
                    Map<Integer, Object> nestedMap = new HashMap<>();
                    parseTlvRecursive(value, value.length(), nestedMap);
                    tlvMap.put(tag, nestedMap);
                } else {
                    tlvMap.put(tag, value);
                }

            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                System.err.println("Malformed TLV Data: " + e.getMessage());
                break;
            }
        }

    }

    // Helper Method to Verify Checksum - Calculate checksum again and compare
//    public boolean verifyChecksum(String userProvidedQrData) {
//        // Extract QR Code data without checksum
//        String qrDataWithoutChecksum = userProvidedQrData.substring(0, userProvidedQrData.length() - 8);
//
//        // Compute checksum again
//        String computedChecksum = CRCUtils.computeCRC(qrDataWithoutChecksum);
//
//        // Extract user-provided checksum
//        String userChecksum = userProvidedQrData.substring(userProvidedQrData.length() - 4);
//
//        return computedChecksum.equals(userChecksum);
//    }

    // Helper Method to Verify Checksum - Checking checksum in DB
    private boolean verifyChecksum(String qrCodeData) {
        // Extract last 4 characters from user-provided QR data
        String userChecksum = qrCodeData.substring(qrCodeData.length() - 4);

        // Retrieve stored checksum from DB
        Optional<QrCodeStorage> storedQrCode = qrCodeStorageRepository.findByQrCodeString(qrCodeData);
        // QR Code not found
        return storedQrCode.filter(qrCodeStorage -> userChecksum.equals(qrCodeStorage.getChecksumValue())).isPresent();

    }

    // Helper Method to Build Error Response
    private VerifyQRCodeResponse buildErrorResponse(VerifyQRCodeRequest request) {
        return VerifyQRCodeResponse.builder()
                .requestMessageId(request.getRequestMessageId())
                .requestDateTime(Instant.now())
                .responseCode("400")
                .responseDescription("Invalid QR Code")
                .validationStatus("Invalid")
                .build();
    }

    private String fetchJourneyTypeName(String journeyCode) {
        return qrFeatureTypeRepository.findJourneyNameByJourneyId(journeyCode);
    }

}
