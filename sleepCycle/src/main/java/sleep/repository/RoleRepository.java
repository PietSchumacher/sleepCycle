package sleep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sleep.models.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
