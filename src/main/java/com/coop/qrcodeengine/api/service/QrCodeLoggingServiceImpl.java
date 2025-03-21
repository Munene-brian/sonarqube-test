package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
import com.coop.qrcodeengine.api.dto.GenerateQRCodeResponse;
import com.coop.qrcodeengine.api.entity.QrCodeRequestsLog;
import com.coop.qrcodeengine.api.repository.QrCodeRequestsLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class QrCodeLoggingServiceImpl implements QrCodeLoggingService {

    private final QrCodeRequestsLogRepository qrCodeRequestsLogRepository;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public QrCodeLoggingServiceImpl(QrCodeRequestsLogRepository qrCodeRequestsLogRepository) {
        this.qrCodeRequestsLogRepository = qrCodeRequestsLogRepository;
    }

    @Override
    public void logRequest(String requestId, GenerateQRCodeRequest request, String requestType) {
        try {
            // Convert request to JSON string
            String requestJson = objectMapper.writeValueAsString(request);

            QrCodeRequestsLog requestLog = new QrCodeRequestsLog();
            requestLog.setRequestId(requestId != null ? requestId : UUID.randomUUID().toString()); // Generate if null
            requestLog.setRequestType(requestType);
            requestLog.setRequestData(requestJson);
            requestLog.setRequestTimestamp(Instant.now());
            requestLog.setStatus("PENDING");
            requestLog.setCreatedAt(Date.from(Instant.now()));
            requestLog.setStatusDescription("Request received, awaiting processing");

            qrCodeRequestsLogRepository.save(requestLog);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to log request data", e);
        }
    }

    @Override
    public void updateLog(String requestId, GenerateQRCodeResponse response, String status, String statusDescription) {
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID cannot be null when updating log");
        }

        try {
            // Convert response to JSON
            String responseJson = objectMapper.writeValueAsString(response);

            Optional<QrCodeRequestsLog> logEntry = qrCodeRequestsLogRepository.findById(requestId);

            logEntry.ifPresent(requestLog -> {
                requestLog.setResponseData(responseJson);
                requestLog.setResponseTimestamp(Instant.now());
                requestLog.setStatus(status);
                requestLog.setStatusDescription(statusDescription);

                qrCodeRequestsLogRepository.save(requestLog);
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to log response data", e);
        }
    }
}