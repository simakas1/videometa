package lt.svaskevicius.videometa.repository;

import java.util.Optional;
import java.util.UUID;
import lt.svaskevicius.videometa.repository.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  Optional<UserEntity> findByUsername(String username);
}
