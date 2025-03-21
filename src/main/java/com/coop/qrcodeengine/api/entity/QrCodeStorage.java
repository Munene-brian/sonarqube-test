package com.coop.qrcodeengine.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "QR_CODE_STORAGE", schema = "MA")
public class QrCodeStorage {
    @Id
    @Size(max = 50)
    @Nationalized
    @Column(name = "QR_CODE_ID", nullable = false, length = 50)
    private String qrCodeId;

    @NotNull
    @Column(name = "QR_CODE_STRING", nullable = false)
    private String qrCodeString;

    @Lob
    @Column(name = "QR_CODE_IMAGE")
    private byte[] qrCodeImage;

    @NotNull
    @Column(name = "CHANNEL_ID", nullable = false)
    private Long channelId;

    @Column(name = "CHECKSUM_VALUE", length = 50) // ✅ Added checksum column
    private String checksumValue;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATED_AT")
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    private Date updatedAt;

    @ColumnDefault("'0'")
    @Column(name = "IS_VALID")
    private Character isValid;

    @Size(max = 20)
    @Nationalized
    @Column(name = "STATUS", length = 20)
    private String status;

}