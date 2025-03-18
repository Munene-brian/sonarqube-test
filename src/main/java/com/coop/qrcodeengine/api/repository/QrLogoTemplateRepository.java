package com.coop.qrcodeengine.api.repository;

import com.coop.qrcodeengine.api.entity.QrLogoTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QrLogoTemplateRepository extends JpaRepository<QrLogoTemplate, Long> {
    Optional<QrLogoTemplate> findByChannelId(Long channelId);
}
