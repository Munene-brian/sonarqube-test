package com.coop.qrcodeengine.api.repository;

import com.coop.qrcodeengine.api.entity.QrCodeStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface QrCodeStorageRepository extends JpaRepository<QrCodeStorage, Long> {

    Optional<QrCodeStorage> findByQrCodeString(String qrCodeData);
}
