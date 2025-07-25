package lt.svaskevicius.videometa.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private UUID id;
    private String username;
    private String password;
    private boolean active;
    private String authorities;
}
