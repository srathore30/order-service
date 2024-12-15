package sfa.order_service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfa.order_service.entity.ReturnStatus;
import sfa.order_service.entity.SalesReturn;

import java.util.Optional;

@Repository
public interface SalesReturnRepo extends JpaRepository<SalesReturn, Long> {
    Optional<SalesReturn> findByOrderEntityId(Long orderId);
    Page<SalesReturn> findByClientFmcgIdAndMemberIdAndReturnStatus(Long clientFmcgId, Long memberId, ReturnStatus returnStatus, Pageable pageable);
}
