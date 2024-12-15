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
   @Query("SELECT DISTINCT o.invoiceNumber " +
           "FROM OrderEntity o " +
           "WHERE o.clientFmcgId = :clientFmcgId AND o.salesLevel = :salesLevel")
   Page<String> findDistinctInvoiceNumbers(@Param("clientFmcgId") Long clientFmcgId,
                                           @Param("salesLevel") SalesLevel salesLevel,
                                           Pageable pageable);
   @Query("SELECT o FROM OrderEntity o WHERE o.invoiceNumber = :invoiceNumber")
   List<OrderEntity> findOrdersByInvoiceNumber(@Param("invoiceNumber") String invoiceNumber);

   List<OrderEntity> findByProductId(Long productId);
   Page<OrderEntity> findByOrderCallStatus(OrderCallStatus orderCallStatus, Pageable pageable);
   @Query("SELECT o FROM OrderEntity o WHERE o.createdDate BETWEEN :startDate AND :endDate AND o.salesLevel = :salesLevel")
   List<OrderEntity> findAllByCreatedDateBetweenAndSalesLevel(@Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate,
                                                              @Param("salesLevel") SalesLevel salesLevel);

   @Query("SELECT o FROM OrderEntity o WHERE o.orderCreatedDate BETWEEN :startDate AND :endDate AND o.memberId = :memberId")
   Page<OrderEntity> findAllByOrderCreatedDateBetweenAndMemberId(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("memberId") Long memberId, Pageable pageable);

   @Query("SELECT o FROM OrderEntity o WHERE o.orderCreatedDate BETWEEN :startDate AND :endDate AND o.memberId = :memberId AND o.beetId = :beetId")
   List<OrderEntity> findAllByOrderCreatedDateBetweenAndMemberIdAndBeetId(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("memberId") Long memberId, @Param("beetId") Long beetId);


   @Query("SELECT o FROM OrderEntity o WHERE o.orderCreatedDate BETWEEN :startDate AND :endDate AND o.beetId = :beetId")
   Page<OrderEntity> findAllByOrderCreatedDateBetweenAndBeetId(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("beetId") Long beetId, Pageable pageable);

   @Query("SELECT o FROM OrderEntity o WHERE o.orderCreatedDate BETWEEN :startDate AND :endDate AND o.outletId = :outletId")
   List<OrderEntity> findAllByOrderCreatedDateBetweenAndOutletId(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("outletId") Long outletId);

   @Query("SELECT o FROM OrderEntity o WHERE o.orderCreatedDate BETWEEN :startDate AND :endDate " + "AND o.memberId IN :memberIds")
   Page<OrderEntity> findOrdersByDateRangeAndMembers(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("memberIds") Set<Long> memberIds, Pageable pageable);

   @Query("SELECT o FROM OrderEntity o WHERE o.orderCreatedDate BETWEEN :startDate AND :endDate " + "AND o.memberId IN :memberIds " + "AND o.beetId = :beetId")
   List<OrderEntity> findOrdersByDateRangeAndMembersAndBeetId(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("memberIds") Set<Long> memberIds, @Param("beetId") Long beetId);
   Page<OrderEntity> findByMemberIdAndOrderCallStatusAndSalesLevelNot(Long memberId, OrderCallStatus orderCallStatus, SalesLevel salesLevel,Pageable pageable);
   Page<OrderEntity> findByMemberIdAndOrderMediumAndSalesLevelNot(Long memberId, OrderMedium orderMedium,SalesLevel salesLevel ,Pageable pageable);
   Page<OrderEntity> findByClientFmcgIdAndOrderCallStatusAndSalesLevelNot(Long clientFmcgId, OrderCallStatus orderCallStatus, SalesLevel salesLevel, Pageable pageable);
   Page<OrderEntity> findByClientFmcgIdAndOrderMediumAndSalesLevelNot(Long clientFmcgId, OrderMedium orderMedium, SalesLevel salesLevel, Pageable pageable);
   Page<OrderEntity> findByMemberId(Long memberId, Pageable pageable);
   Page<OrderEntity> findByClientFmcgId(Long clientFmcgId, Pageable pageable);
   Page<OrderEntity> findByClientFmcgIdAndSalesLevel(Long clientFmcgId, SalesLevel salesLevel, Pageable pageable);
   @Query("SELECT o FROM OrderEntity o WHERE o.salesLevel = :salesLevel AND o.memberId IN :memberIds")
   Page<OrderEntity> findByMembersIdList(@Param("memberIds") Set<Long> memberIds,@Param("salesLevel") SalesLevel salesLevel, Pageable pageable);

   List<OrderEntity> findByInvoiceNumber(String invoiceNumber);
   long countByOutletIdAndMemberIdAndOrderMedium(Long outLetId, Long memberId, OrderMedium orderMedium);
   long countByOutletIdAndMemberIdAndOrderCallStatus(Long outLetId, Long memberId, OrderCallStatus orderCallStatus);
   long countByBeetIdAndMemberIdAndOrderCallStatus(Long beetId, Long memberId, OrderCallStatus orderCallStatus);
   long countByBeetIdAndMemberIdAndOrderMedium(Long beetId, Long memberId, OrderMedium orderMedium);


   long countByOutletIdAndClientFmcgIdAndOrderMedium(Long outLetId, Long clientFmcgId, OrderMedium orderMedium);
   long countByOutletIdAndClientFmcgIdAndOrderCallStatus(Long outLetId, Long clientFmcgId, OrderCallStatus orderCallStatus);
   long countByBeetIdAndClientFmcgIdAndOrderCallStatus(Long beetId, Long clientFmcgId, OrderCallStatus orderCallStatus);
   long countByBeetIdAndClientFmcgIdAndOrderMedium(Long beetId, Long clientFmcgId, OrderMedium orderMedium);

}
