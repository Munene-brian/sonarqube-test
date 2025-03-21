package com.coop.qrcodeengine.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "QR_TLV_SUBTEMPLATE", schema = "MA")
public class QrTlvSubtemplate {
    @Id
    @Column(name = "SUB_TAG_SEQUENCE", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "SUB_TAG_ID", nullable = false)
    private Integer subTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "PARENT_TEMPLATE_TAG_ID", referencedColumnName = "TAG_ID"),
            @JoinColumn(name = "TEMPLATE_ID", referencedColumnName = "TEMPLATE_ID"),
            @JoinColumn(name = "CHANNEL_ID", referencedColumnName = "CHANNEL_ID")
    })
    @OnDelete(action = OnDeleteAction.CASCADE)
    private QrTlvTemplate qrTlvTemplate;

    @Column(name = "PARENT_SUB_TAG_ID", insertable = false, updatable = false)
    private Integer parentSubTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "PARENT_SUB_TAG_ID")
    private QrTlvSubtemplate parentSubTag;

    @ColumnDefault("0")
    @Column(name = "MIN_LENGTH")
    private Long minLength;

    @ColumnDefault("0")
    @Column(name = "MAX_LENGTH")
    private Long maxLength;

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