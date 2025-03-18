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
@Table(name = "QR_CODE_DETAILS", schema = "MA")
public class QrCodeDetails {

    @EmbeddedId
    private QrCodeDetailsId id;

    @Size(max = 50)
    @Nationalized
    @Column(name = "QR_CODE_TYPE", length = 50)
    private String qrCodeType;

    @NotNull
    @Column(name = "CHANNEL_ID", nullable = false)
    private Long channelId;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "FIELD_VALUE", length = 1000)
    private String fieldValue;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATED_AT")
    private Date createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "UPDATED_AT")
    private Date updatedAt;

    // Establish Relationship to QrCodeStorage
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QR_CODE_ID", referencedColumnName = "QR_CODE_ID", insertable = false, updatable = false)
    private QrCodeStorage qrCodeStorage;
}