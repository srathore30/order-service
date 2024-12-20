package sfa.order_service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sfa.order_service.entity.OrderInvoice;
import sfa.order_service.enums.SalesLevel;

import java.util.List;
import java.util.Set;

@Repository
public interface OrderInvoicesRepo extends JpaRepository<OrderInvoice, Long> {
    Page<OrderInvoice> findByClientFmcgIdAndSalesLevel(Long clientFmcgId, SalesLevel salesLevel, Pageable pageable);
    @Query("SELECT o FROM OrderInvoice o " + "WHERE o.salesLevel = :salesLevel " + "AND o.memberId IN :memberIds ")
    Page<OrderInvoice> findByReportingManagerMembersAndSalesLevel(@Param("salesLevel") SalesLevel salesLevel, @Param("memberIds") Set<Long> memberIds, Pageable pageable);
}
