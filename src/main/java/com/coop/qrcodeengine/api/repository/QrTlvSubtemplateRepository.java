package com.coop.qrcodeengine.api.repository;

import com.coop.qrcodeengine.api.entity.QrTlvSubtemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QrTlvSubtemplateRepository extends JpaRepository<QrTlvSubtemplate, Long> {
//    List<QrTlvSubtemplate> findByQrTlvTemplateId(@NotNull Integer tagId, @NotNull Integer templateId, @NotNull Integer channelId);
@Query("SELECT s FROM QrTlvSubtemplate s WHERE s.qrTlvTemplate.id.tagId = :tagId " +
        "AND s.qrTlvTemplate.id.templateId = :templateId " +
        "AND s.qrTlvTemplate.id.channelId = :channelId " +
        "AND s.verifyJson = '1'")
List<QrTlvSubtemplate> findByParentTemplateTagId(@Param("tagId") Integer parentTagId,
                                                 @Param("templateId") Integer templateId,
                                                 @Param("channelId") Integer channelId);



    @Query("SELECT s FROM QrTlvSubtemplate s WHERE s.parentSubTag.id = :tagId " +
            "AND s.qrTlvTemplate.id.templateId = :templateId " +
            "AND s.qrTlvTemplate.id.channelId = :channelId " +
            "AND s.verifyJson = '1'")
    List<QrTlvSubtemplate> findByParentSubTagId(@Param("tagId") Integer parentSubTagId,
                                                @Param("templateId") Integer templateId,
                                                @Param("channelId") Integer channelId);

    @Query("SELECT CASE WHEN COUNT(q) > 0 THEN true ELSE false END FROM QrTlvSubtemplate q WHERE q.subTagId = :tag AND q.hasChild = '1'")
    boolean hasChild(@Param("tag") int tag);

}
