package sfa.order_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sfa.order_service.entity.TransactionEntity;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
//   Page<OrderEntity> findById(Long orderId, Pageable pageable);
   List<TransactionEntity> findByClientId(Long clientId);

   @Modifying
   @Transactional
   @Query("DELETE FROM TransactionEntity t WHERE t.orderId IN :ids")
   void deleteAllByOrderId(@Param("ids") List<Long> ids);
   TransactionEntity findByClientIdAndOrderId(Long clientId, Long orderId);

}
