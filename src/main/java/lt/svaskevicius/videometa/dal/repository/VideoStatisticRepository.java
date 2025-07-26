package lt.svaskevicius.videometa.dal.repository;

import java.util.List;
import lt.svaskevicius.videometa.dal.model.VideoStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoStatisticRepository extends JpaRepository<VideoStatistics, String> {

  @Query(value = "SELECT * FROM video_stats_per_source", nativeQuery = true)
  List<VideoStatistics> findAllStats();
}