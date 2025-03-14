//package com.coop.qrcodeengine.api.entity;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import jakarta.validation.constraints.Size;
//import lombok.Getter;
//import lombok.Setter;
//import org.hibernate.annotations.ColumnDefault;
//import org.hibernate.annotations.Nationalized;
//
//import java.util.Date;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "DYNAMIC_QRCODEDATA_TLVTEMPLATE", schema = "QR")
//public class DynamicQrCodeTemplate {
//    @Id
//    @Column(name = "TAG_ID", nullable = false)
//    private Integer id;
//
//    @ColumnDefault("'0'")
//    @Column(name = "MIN_LENGTH")
//    private Long minLength;
//
//    @ColumnDefault("'0'")
//    @Column(name = "MAX_LENGTH")
//    private Long maxLength;
//
//    @Size(max = 200)
//    @Nationalized
//    @Column(name = "TAG_GROUP", length = 200)
//    private String tagGroup;
//
//    @Size(max = 200)
//    @Nationalized
//    @Column(name = "CONTENT_DESC", length = 200)
//    private String contentDesc;
//
//    @Size(max = 200)
//    @Nationalized
//    @Column(name = "CONTENT_VALUE", length = 200)
//    private String contentValue;
//
//    @Size(max = 3)
//    @ColumnDefault("'S'")
//    @Column(name = "FORMAT")
//    private Character format;
//
//    @Column(name = "IS_STATIC")
//    private Character isStatic;
//
//    @ColumnDefault("'0'")
//    @Column(name = "REQUIRED")
//    private Character required;
//
//    @Size(max = 1500)
//    @Nationalized
//    @Column(name = "USAGE", length = 1500)
//    private String usage;
//
//    @ColumnDefault("CURRENT_TIMESTAMP")
//    @Column(name = "DATE_CREATED")
//    private Date dateCreated;
//
//    @ColumnDefault("CURRENT_TIMESTAMP")
//    @Column(name = "DATE_LAST_UPDATED")
//    private Date dateLastUpdated;
//
//    @ColumnDefault("'1'")
//    @Column(name = "VALID")
//    private Character valid;
//}
