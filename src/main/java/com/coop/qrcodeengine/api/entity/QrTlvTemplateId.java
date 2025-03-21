package com.coop.qrcodeengine.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class QrTlvTemplateId implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 5850744358004128709L;
    @NotNull
    @Column(name = "TAG_ID", nullable = false)
    private Integer tagId;

    @NotNull
    @Column(name = "TEMPLATE_ID", nullable = false)
    private Integer templateId;

    @NotNull
    @Column(name = "CHANNEL_ID", nullable = false)
    private Integer channelId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        QrTlvTemplateId entity = (QrTlvTemplateId) o;
        return Objects.equals(this.tagId, entity.tagId) &&
                Objects.equals(this.templateId, entity.templateId) &&
                Objects.equals(this.channelId, entity.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, templateId, channelId);
    }

}