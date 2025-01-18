package sfa.order_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfa.order_service.entity.AchievementEntity;
@Repository
public interface AchievementRepo extends JpaRepository<AchievementEntity, Long> {
}
