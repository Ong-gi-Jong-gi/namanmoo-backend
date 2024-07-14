package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {
}
