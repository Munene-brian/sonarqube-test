package com.coop.qrcodeengine.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class QrCodeDetailsId implements Serializable {

    @Size(max = 50)
    @Column(name = "QR_CODE_ID", nullable = false, length = 50)
    private String qrCodeId;

    @Size(max = 50)
    @Column(name = "FIELD_NAME", nullable = false, length = 50)
    private String fieldName;
}
