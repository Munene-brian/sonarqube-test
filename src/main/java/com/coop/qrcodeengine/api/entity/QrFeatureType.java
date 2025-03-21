package com.coop.qrcodeengine.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "QR_FEATURE_TYPE", schema = "MA")
public class QrFeatureType {
    @Id
    @Size(max = 10)
    @Nationalized
    @Column(name = "JOURNEY_ID", nullable = false, length = 10)
    private String journeyId;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "JOURNEY_NAME", nullable = false, length = 100)
    private String journeyName;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATED_AT")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "UPDATED_AT")
    private Instant updatedAt;

}