package lt.svaskevicius.videometa.service.video;

import java.time.LocalDate;
import lt.svaskevicius.videometa.dal.model.Video;
import lt.svaskevicius.videometa.web.model.video.VideoFilterDto;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class VideoSpecificationService {

  private static final String PARAM_SOURCE = "source";
  private static final String PARAM_UPLOAD_DATE = "uploadDate";
  private static final String PARAM_DURATION = "duration";

  public static Specification<Video> hasSource(final String source) {
    return (root, query, criteriaBuilder) -> {
      if (source == null || source.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.like(
          criteriaBuilder.lower(root.get(PARAM_SOURCE)),
          "%" + source.toLowerCase() + "%"
      );
    };
  }

  public static Specification<Video> hasUploadDateFrom(final LocalDate uploadDateFrom) {
    return (root, query, criteriaBuilder) -> {
      if (uploadDateFrom == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.greaterThanOrEqualTo(root.get(PARAM_UPLOAD_DATE), uploadDateFrom);
    };
  }

  public static Specification<Video> hasUploadDateTo(final LocalDate uploadDateTo) {
    return (root, query, criteriaBuilder) -> {
      if (uploadDateTo == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.lessThanOrEqualTo(root.get(PARAM_UPLOAD_DATE), uploadDateTo);
    };
  }

  public static Specification<Video> hasDurationFrom(final Integer durationFrom) {
    return (root, query, criteriaBuilder) -> {
      if (durationFrom == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.greaterThanOrEqualTo(root.get(PARAM_DURATION), durationFrom);
    };
  }

  public static Specification<Video> hasDurationTo(final Integer durationTo) {
    return (root, query, criteriaBuilder) -> {
      if (durationTo == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.lessThanOrEqualTo(root.get("PARAM_DURATION"), durationTo);
    };
  }

  public static Specification<Video> buildSpecification(final VideoFilterDto filterDto) {
    Specification<Video> spec = hasSource(filterDto.source());

    spec = spec.and(hasUploadDateFrom(filterDto.uploadDateFrom()));
    spec = spec.and(hasUploadDateTo(filterDto.uploadDateTo()));
    spec = spec.and(hasDurationFrom(filterDto.durationFrom()));
    spec = spec.and(hasDurationTo(filterDto.durationTo()));

    return spec;
  }
}