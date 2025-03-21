package com.coop.qrcodeengine.api.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private static final DateTimeFormatter QR_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy'T'HHmmss");

    /**
     * Generates a timestamp in the required QR format (DDMMYYYYTHHMMSS)
     * @return Formatted timestamp as a String
     */
    public static String generateQrTimestamp() {
        return LocalDateTime.now().format(QR_TIMESTAMP_FORMAT);
    }
}
