package lt.svaskevicius.videometa.dal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;

@Data
@Entity
@Immutable
@Table(name = "video_stats_per_source")
public class VideoStatistics {

  @Id
  private String source;

  @Column(name = "total_videos")
  private Long totalVideos;

  @Column(name = "average_duration")
  private Double averageDuration;
}
