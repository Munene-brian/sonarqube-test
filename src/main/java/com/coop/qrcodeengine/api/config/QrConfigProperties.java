package com.coop.qrcodeengine.api.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class QrConfigProperties {

    @Value("${qr.config.keqr-subdomain}")
    private String keQrSubDomain;

}
