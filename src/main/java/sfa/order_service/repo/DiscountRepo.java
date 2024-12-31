package sfa.order_service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sfa.order_service.entity.DiscountEntity;

import java.util.Date;
import java.util.List;

@Repository
public interface DiscountRepo extends JpaRepository<DiscountEntity, Long> {
    @Query("SELECT d FROM DiscountEntity d WHERE " + "(d.productId IS NULL OR d.productId = :productId) AND " + "(d.outletId IS NULL OR d.outletId = :outletId) AND " + ":orderDate BETWEEN d.validFrom AND d.validTo")
    List<DiscountEntity> findApplicableDiscounts(@Param("productId") Long productId, @Param("outletId") Long outletId, @Param("orderDate") Date orderDate);

    DiscountEntity findByDiscountCode(String discountCode);

    Page<DiscountEntity> findAllByProductId(Long productId, Pageable pageable);

    List<DiscountEntity> findByDiscountCodeAndProductIdAndOutletId(String discountCode, Long productId, Long outletId);

    boolean existsByDiscountCode(String discountCode);

}
