package ongjong.namanmoo.repository;

import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {
    List<SharedFile> findByChallengeNumAndLucky(int challengeNum, Lucky lucky);

    SharedFile findByFileName(String fileName);
}
