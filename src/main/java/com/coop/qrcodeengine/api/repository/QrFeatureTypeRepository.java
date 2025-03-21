package com.coop.qrcodeengine.api.repository;

import com.coop.qrcodeengine.api.entity.QrFeatureType;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QrFeatureTypeRepository extends JpaRepository<QrFeatureType, Integer> {
    @Query("SELECT q.journeyName FROM QrFeatureType q WHERE q.journeyId = :journeyId")
    String findJourneyNameByJourneyId(@Size(max = 10) String journeyId);
}
