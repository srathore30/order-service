package sfa.order_service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import sfa.order_service.entity.OrderEntity;
import sfa.order_service.enums.SalesLevel;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
   Page<OrderEntity> findById(Long orderId, Pageable pageable);
   List<OrderEntity> findByProductId(Long productId);
   @Query("SELECT o FROM OrderEntity o WHERE o.createdDate BETWEEN :startDate AND :endDate AND o.salesLevel = :salesLevel")
   List<OrderEntity> findAllByCreatedDateBetweenAndSalesLevel(@Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate,
                                                              @Param("salesLevel") SalesLevel salesLevel);

}
