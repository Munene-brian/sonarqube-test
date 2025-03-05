package com.coop.qrcodeengine.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QrcodeEngineApplication {
    private static final Logger LOGGER = LogManager.getLogger(QrcodeEngineApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(QrcodeEngineApplication.class, args);
        LOGGER.info("🚀 QR Code Engine Started Successfully!");
    }

}
