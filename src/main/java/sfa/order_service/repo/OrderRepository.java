package sfa.order_service.repo;

import org.hibernate.query.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sfa.order_service.constant.OrderCallStatus;
import sfa.order_service.constant.OrderMedium;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.enums.SalesLevel;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
   Page<OrderEntity> findById(Long orderId, Pageable pageable);
   List<OrderEntity> findByProductId(Long productId);
   Page<OrderEntity> findByOrderCallStatus(OrderCallStatus orderCallStatus, Pageable pageable);
   @Query("SELECT o FROM OrderEntity o WHERE o.createdDate BETWEEN :startDate AND :endDate AND o.salesLevel = :salesLevel")
   List<OrderEntity> findAllByCreatedDateBetweenAndSalesLevel(@Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate,
                                                              @Param("salesLevel") SalesLevel salesLevel);

   @Query("SELECT o FROM OrderEntity o WHERE o.orderCreatedDate BETWEEN :startDate AND :endDate AND o.memberId = :memberId")
   Page<OrderEntity> findAllByOrderCreatedDateBetweenAndMemberId(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("memberId") Long memberId, Pageable pageable);

   @Query("SELECT o FROM OrderEntity o WHERE o.orderCreatedDate BETWEEN :startDate AND :endDate AND o.beetId = :beetId")
   Page<OrderEntity> findAllByOrderCreatedDateBetweenAndBeetId(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("beetId") Long beetId, Pageable pageable);

   @Query("SELECT o FROM OrderEntity o WHERE o.orderCreatedDate BETWEEN :startDate AND :endDate " + "AND o.memberId IN :memberIds")
   Page<OrderEntity> findOrdersByDateRangeAndMembers(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("memberIds") Set<Long> memberIds, Pageable pageable);
   Page<OrderEntity> findByMemberIdAndOrderCallStatus(Long memberId, OrderCallStatus orderCallStatus, Pageable pageable);
   Page<OrderEntity> findByMemberIdAndOrderMedium(Long memberId, OrderMedium orderMedium, Pageable pageable);
   Page<OrderEntity> findByClientFmcgIdAndOrderCallStatus(Long clientFmcgId, OrderCallStatus orderCallStatus, Pageable pageable);
   Page<OrderEntity> findByClientFmcgIdAndOrderMedium(Long clientFmcgId, OrderMedium orderMedium, Pageable pageable);
   Page<OrderEntity> findByMemberId(Long memberId, Pageable pageable);
   Page<OrderEntity> findByClientFmcgId(Long clientFmcgId, Pageable pageable);
   @Query("SELECT o FROM OrderEntity o WHERE o.memberId IN :memberIds")
   Page<OrderEntity> findByMembersIdList(@Param("memberIds") Set<Long> memberIds, Pageable pageable);

}
