package com.coop.qrcodeengine.api.repository;

import com.coop.qrcodeengine.api.entity.QrTlvTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QrTlvTemplateRepository extends JpaRepository<QrTlvTemplate, Long> {
    List<QrTlvTemplate> findByIsStatic(Character isStatic);
    List<QrTlvTemplate> findByIsDynamic(Character isDynamic);

    @Query("SELECT t FROM QrTlvTemplate t WHERE t.id.channelId = :channelId " +
            "AND t.verifyJson = '1' " +
            "AND ((:isStatic = '1' AND t.isStatic = '1') OR (:isDynamic = '1' AND t.isDynamic = '1'))")
    List<QrTlvTemplate> findByChannelIdAndType(@Param("channelId") Integer channelId,
                                               @Param("isStatic") String isStatic,
                                               @Param("isDynamic") String isDynamic);

    @Query("SELECT CASE WHEN COUNT(q) > 0 THEN true ELSE false END FROM QrTlvTemplate q WHERE q.id.tagId = :tag AND q.hasChild = '1'")
    boolean hasChild(@Param("tag") int tag);

}
