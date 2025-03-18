package com.coop.qrcodeengine.api.utils;

import com.coop.qrcodeengine.api.entity.QrTlvTemplate;
import com.coop.qrcodeengine.api.entity.QrTlvSubtemplate;
import com.coop.qrcodeengine.api.repository.QrTlvSubtemplateRepository;
import org.springframework.stereotype.Component;

import java.util.*;

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
            String value = getValueForTag(template, qrData, isDynamic);

            List<QrTlvSubtemplate> subTemplates = subTemplateMap.getOrDefault(template.getId().getTagId(), Collections.emptyList());
            String nestedSubTemplate = buildNestedTags(subTemplates, qrData, subTemplateMap);

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

    private String buildNestedTags(List<QrTlvSubtemplate> subTemplates, Map<String, Object> qrData,
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
                value = getValueFromTemplateOrUser(subTemplate, qrData, subTemplateMap);
            }

            if (value == null) {
                continue;
            }

            List<QrTlvSubtemplate> childSubTemplates = subTemplateMap.getOrDefault(subTemplate.getSubTagId(), Collections.emptyList());
            String nestedChildSubTemplate = buildNestedTags(childSubTemplates, qrData, subTemplateMap);

            if (!nestedChildSubTemplate.isEmpty()) {
                value += nestedChildSubTemplate;
            }

            String length = String.format("%02d", value.length());
            nestedString.append(tag).append(length).append(value);
        }

        return nestedString.toString();
    }

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

    private static String getValueForTag(QrTlvTemplate template, Map<String, Object> qrData, boolean isDynamic) {
        // Special handling for qr type field (Tag 01)
        if (template.getId().getTagId() == 1) {
            return isDynamic ? "12" : "11";
        }

        if (template.getContentValue() != null) {
            return template.getContentValue();
        }

        return qrData.getOrDefault(template.getJsonKey(), null) != null
                ? qrData.get(template.getJsonKey()).toString()
                : null;

    }

    private static String getValueFromTemplateOrUser(QrTlvSubtemplate subTemplate, Map<String, Object> qrData, Map<Integer, List<QrTlvSubtemplate>> subTemplateMap) {
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
}
