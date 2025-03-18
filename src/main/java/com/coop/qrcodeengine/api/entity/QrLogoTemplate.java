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
@Table(name = "QR_LOGO_TEMPLATES", schema = "MA")
public class QrLogoTemplate {
    @Id
    @Column(name = "TEMPLATE_ID", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "CHANNEL_ID", nullable = false)
    private Long channelId;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "TEMPLATE_NAME", nullable = false, length = 100)
    private String templateName;

    @Lob // Maps BLOB column
    @NotNull
    @Column(name = "TEMPLATE_IMAGE", columnDefinition = "BLOB", nullable = false)
    private byte[] templateImage;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATED_AT")
    private Date createdAt;

}