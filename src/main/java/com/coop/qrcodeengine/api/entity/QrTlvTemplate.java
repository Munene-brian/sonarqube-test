package com.coop.qrcodeengine.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "QR_TLV_TEMPLATE", schema = "MA")
public class QrTlvTemplate {
    @EmbeddedId
    private QrTlvTemplateId id;

    @ColumnDefault("0")
    @Column(name = "MIN_LENGTH")
    private Long minLength;

    @ColumnDefault("0")
    @Column(name = "MAX_LENGTH")
    private Long maxLength;

    @Size(max = 200)
    @Nationalized
    @Column(name = "TAG_GROUP", length = 200)
    private String tagGroup;

    @Size(max = 200)
    @Nationalized
    @Column(name = "CONTENT_DESC", length = 200)
    private String contentDesc;

    @Size(max = 200)
    @Nationalized
    @Column(name = "JSON_KEY", length = 200)
    private String jsonKey;

    @Size(max = 200)
    @Nationalized
    @Column(name = "CONTENT_VALUE", length = 200)
    private String contentValue;

    @Size(max = 3)
    @ColumnDefault("'S'")
    @Column(name = "FORMAT", length = 3)
    private Character format;

    @Column(name = "IS_STATIC")
    private Character isStatic;

    @ColumnDefault("'0'")
    @Column(name = "IS_DYNAMIC")
    private Character isDynamic;

    @ColumnDefault("'0'")
    @Column(name = "REQUIRED")
    private Character required;

    @ColumnDefault("'0'")
    @Column(name = "VERIFY_JSON", length = 1)
    private Character verifyJson;

    @ColumnDefault("'0'")
    @Column(name = "HAS_CHILD", length = 1)
    private Character hasChild;

    @Size(max = 1500)
    @Nationalized
    @Column(name = "USAGE", length = 1500)
    private String usage;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATED_AT")
    private Date createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "UPDATED_AT")
    private Date updatedAt;

    @ColumnDefault("'1'")
    @Column(name = "VALID")
    private Character valid;

}