package com.coop.qrcodeengine.api.repository;

import com.coop.qrcodeengine.api.entity.QrTlvSubtemplate;
import com.coop.qrcodeengine.api.entity.QrTlvTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QrTlvSubtemplateRepository extends JpaRepository<QrTlvSubtemplate, Long> {
    List<QrTlvSubtemplate> findByQrTlvTemplate(QrTlvTemplate qrTlvTemplate);
}
