package com.coop.qrcodeengine.api.utils;

public class CRCUtils {

    private static final int POLYNOMIAL = 0x1021; // Standard CRC-16/CCITT-FALSE polynomial
    private static final int INITIAL_VALUE = 0xFFFF; // Initial CRC value

    /**
     * Computes CRC-16/CCITT-FALSE checksum.
     *
     * @param inputData The QR code data string (excluding CRC tag).
     * @return The computed 4-character CRC checksum in hexadecimal.
     */
    public static String computeCRC(String inputData) {
        int crc = INITIAL_VALUE;

        byte[] bytes = inputData.getBytes(); // Convert input string to bytes

        for (byte b : bytes) {
            crc ^= (b << 8); // XOR byte with upper byte of crc

            for (int i = 0; i < 8; i++) { // Process 8 bits
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ POLYNOMIAL;
                } else {
                    crc <<= 1;
                }
            }
        }

        // Convert CRC to uppercase hexadecimal string (4 characters)
        return String.format("%04X", crc & 0xFFFF);
    }
}
