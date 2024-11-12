package sfa.order_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfa.order_service.entity.OrderItems;
@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
}