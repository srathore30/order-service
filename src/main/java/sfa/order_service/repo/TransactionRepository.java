package sfa.order_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfa.order_service.entity.TransactionEntity;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
//   Page<OrderEntity> findById(Long orderId, Pageable pageable);
   List<TransactionEntity> findByClientId(Long clientId);
   void deleteAllByOrderId(List<Long> ids);

   TransactionEntity findByClientIdAndOrderId(Long clientId, Long orderId);

}
