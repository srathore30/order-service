package sfa.order_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfa.order_service.entity.InvoiceMaster;


@Repository
public interface InvoiceMasterRepo extends JpaRepository<InvoiceMaster, Long> {
}
