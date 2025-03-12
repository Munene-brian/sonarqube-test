package com.coop.qrcodeengine.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.util.Date;

@Data
@Entity
@Table(name = "QRCODE_DETAILS", schema = "QR")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrCodeDetails {
    @Id
    @Size(max = 50)
    @Nationalized
    @ColumnDefault("SYS_GUID()")
    @Column(name = "QRCODEID", nullable = false, length = 50)
    private String qrCodeId;

    @Size(max = 50)
    @Nationalized
    @Column(name = "QRCODETYPE", length = 50)
    private String qrCodeType;

    @Size(max = 10)
    @Nationalized
    @Column(name = "QRCODEVERSION", length = 10)
    private String qrCodeVersion;

    @Size(max = 50)
    @Nationalized
    @Column(name = "MERCHANTSNAME", length = 50)
    private String merchantsName;

    @Size(max = 10)
    @Nationalized
    @Column(name = "COUNTRYCODE", length = 10)
    private String countryCode;

    @Size(max = 50)
    @Nationalized
    @Column(name = "MERCHANTCITY", length = 50)
    private String merchantCity;

    @Size(max = 50)
    @Nationalized
    @Column(name = "POSTALCODE", length = 50)
    private String postalCode;

    @Size(max = 50)
    @Nationalized
    @Column(name = "MERCHANTACCOUNTINFORMATION", length = 50)
    private String merchantAccountInformation;

    @Size(max = 50)
    @Nationalized
    @Column(name = "MERCHANTCATEGORYCODE", length = 50)
    private String merchantCategoryCode;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATIONDATE")
    private Date creationDate;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "MODIFIEDDATE")
    private Date modifiedDate;

    @Size(max = 10)
    @Nationalized
    @Column(name = "TRANSACTIONCURRENCY", length = 10)
    private String transactionCurrency;

    @Column(name = "TRANSACTIONAMOUNT")
    private Long transactionAmount;

    @Column(name = "CONVINIENCEINDICATOR")
    private Long convinienceIndicator;

    @Column(name = "CONVINIENCEFEEFIXED")
    private Long convinienceFeeFixed;

    @Column(name = "CONVINIENCEFEEPERCENTAGE")
    private Long convinienceFeePercentage;

    @Lob
    @Column(name = "QRCODEDATA")
    private String qrCodeData;

    @Lob
    @Column(name = "QRCODEIMAGE")
    private String qrCodeImage;

    @Size(max = 50)
    @Nationalized
    @Column(name = "CHECKSUMVALUE", length = 50)
    private String checkSumValue;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ADDITIONALINFORMATION", length = 100)
    private String additionalInformation;

    @Size(max = 50)
    @Nationalized
    @Column(name = "MESSAGEID", length = 50)
    private String messageId;

}