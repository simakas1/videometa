package lt.svaskevicius.videometa.dal.repository;

import java.util.UUID;
import lt.svaskevicius.videometa.dal.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID>, JpaSpecificationExecutor<Video> {

  @Modifying
  @Transactional
  @Query(value = """
      INSERT INTO videos (id, title, url, duration, source, upload_date, created_at, updated_at)
      VALUES (gen_random_uuid(), :#{#video.title}, :#{#video.url}, :#{#video.duration},:#{#video.source},:#{#video.uploadDate},
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      ON CONFLICT (url)
      DO UPDATE SET
          title = EXCLUDED.title,
          duration = EXCLUDED.duration,
          source = EXCLUDED.source,
          upload_date = EXCLUDED.upload_date,
          updated_at = CURRENT_TIMESTAMP
      """, nativeQuery = true)
  void upsertVideo(@Param("video") Video video);
}
