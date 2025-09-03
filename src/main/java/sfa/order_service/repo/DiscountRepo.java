package sfa.order_service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfa.order_service.constant.DiscountType;
import sfa.order_service.entity.DiscountEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepo extends JpaRepository<DiscountEntity, Long> {
    DiscountEntity findByDiscountCode(String discountCode);

    Page<DiscountEntity> findAllByProductId(Long productId, Pageable pageable);

    boolean existsByDiscountCode(String discountCode);

    List<DiscountEntity> findByDiscountCodeAndProductId(String discountCode, Long productId);
    Optional<DiscountEntity> findByProductId(Long productId);

    Page<DiscountEntity> findByStateAndCityAndDiscountType(String state, String city, DiscountType discountType, Pageable pageable);
}
