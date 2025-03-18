package com.coop.qrcodeengine.api.repository;

import com.coop.qrcodeengine.api.entity.QrCodeRequestsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrCodeRequestsLogRepository extends JpaRepository<QrCodeRequestsLog, String> {
}
