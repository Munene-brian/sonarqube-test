package com.coop.qrcodeengine.api.service;

import com.coop.qrcodeengine.api.dto.GenerateQRCodeRequest;
import com.coop.qrcodeengine.api.entity.QrCodeRequestLog;
import com.coop.qrcodeengine.api.repository.QrCodeRequestLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class QrCodeLoggingServiceImpl implements QrCodeLoggingService {

    private final QrCodeRequestLogRepository qrCodeRequestLogRepository;

    public QrCodeLoggingServiceImpl(QrCodeRequestLogRepository qrCodeRequestLogRepository) {
        this.qrCodeRequestLogRepository = qrCodeRequestLogRepository;
    }

    @Override
    public void logRequest(String requestId, GenerateQRCodeRequest request, String requestType) {
        QrCodeRequestLog requestLog = QrCodeRequestLog.builder()
                .requestId(requestId != null ? requestId : UUID.randomUUID().toString()) // Generate ID if not provided
                .requestData(request.toString())
                .requestTimestamp(Date.from(Instant.now()))
                .status("PENDING") // Log as PENDING until response is set
                .statusDescription("Request received, awaiting processing")
                .requestType(requestType)
                .build();

        qrCodeRequestLogRepository.save(requestLog);
    }

    @Override
    public void updateLog(String requestId, String responseData) {
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID cannot be null when updating log");
        }

        qrCodeRequestLogRepository.findById(requestId).ifPresent(requestLog -> {
            requestLog.setResponseData(responseData);
            requestLog.setResponseTimestamp(Date.from(Instant.now()));
            requestLog.setStatus("SUCCESS");
            requestLog.setStatusDescription("QR Code generated successfully");
            qrCodeRequestLogRepository.save(requestLog);
        });
    }
}
