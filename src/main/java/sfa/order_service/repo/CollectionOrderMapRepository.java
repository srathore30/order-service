package sfa.order_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfa.order_service.entity.CollectionOrderMap;

import java.util.Optional;

@Repository
public interface CollectionOrderMapRepository extends JpaRepository<CollectionOrderMap, Long> {}
