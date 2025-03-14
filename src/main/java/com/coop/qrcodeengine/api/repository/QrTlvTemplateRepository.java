package com.coop.qrcodeengine.api.repository;

import com.coop.qrcodeengine.api.entity.QrTlvTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QrTlvTemplateRepository extends JpaRepository<QrTlvTemplate, Long> {
    List<QrTlvTemplate> findByIsStatic(Character isStatic);
    List<QrTlvTemplate> findByIsDynamic(Character isDynamic);
}
