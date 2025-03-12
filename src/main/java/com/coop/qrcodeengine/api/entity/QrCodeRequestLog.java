package com.coop.qrcodeengine.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;

@Data
@Entity
@Table(name = "QRCODE_REQUESTS_LOG", schema = "QR")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrCodeRequestLog {
    @Id
    @Size(max = 50)
    @ColumnDefault("SYS_GUID()")
    @Column(name = "REQUEST_ID", nullable = false, length = 50)
    private String requestId;

    @NotNull
    @Lob
    @Column(name = "REQUEST_DATA", nullable = false)
    private String requestData;

    @Lob
    @Column(name = "RESPONSE_DATA")
    private String responseData;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "REQUEST_TIMESTAMP", nullable = false)
    private Date requestTimestamp;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "RESPONSE_TIMESTAMP")
    private Date responseTimestamp;

    @Size(max = 20)
    @Column(name = "STATUS", length = 20)
    private String status;

    @Size(max = 220)
    @Column(name = "STATUS_DESCRIPTION", length = 220)
    private String statusDescription;

    @Size(max = 100)
    @Column(name = "REQUEST_TYPE", length = 100)
    private String requestType;

}