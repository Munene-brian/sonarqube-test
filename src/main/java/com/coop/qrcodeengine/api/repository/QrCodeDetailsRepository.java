package com.coop.qrcodeengine.api.repository;

import com.coop.qrcodeengine.api.entity.QrCodeDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrCodeDetailsRepository extends JpaRepository<QrCodeDetails, String> {
}
