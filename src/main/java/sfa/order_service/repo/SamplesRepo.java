package sfa.order_service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.entity.SamplesEntity;
import sfa.order_service.enums.SalesLevel;

import java.util.List;
import java.util.Set;

@Repository
public interface SamplesRepo extends JpaRepository<SamplesEntity, Long> {
    Page<SamplesEntity> findByMemberId(Long memberId, Pageable pageable);
    @Query("SELECT o FROM SamplesEntity o WHERE o.memberId IN :memberIds")
    Page<SamplesEntity> findByMembersIdList(@Param("memberIds") Set<Long> memberIds, Pageable pageable);

}
