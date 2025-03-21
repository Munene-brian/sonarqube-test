package com.coop.qrcodeengine.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "QR_CODE_REQUESTS_LOG", schema = "MA")
public class QrCodeRequestsLog {
    @Id
    @Size(max = 50)
    @Nationalized
    @ColumnDefault("SYS_GUID()")
    @Column(name = "REQUEST_ID", nullable = false, length = 50)
    private String requestId;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "REQUEST_TYPE", nullable = false, length = 100)
    private String requestType;

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
    private Instant requestTimestamp;

    @ColumnDefault("NULL")
    @Column(name = "RESPONSE_TIMESTAMP")
    private Instant responseTimestamp;

    @Size(max = 20)
    @Nationalized
    @Column(name = "STATUS", length = 20)
    private String status;

    @Size(max = 220)
    @Nationalized
    @Column(name = "STATUS_DESCRIPTION", length = 220)
    private String statusDescription;

    @Column(name = "CREATED_AT")
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    private Date updatedAt;

}