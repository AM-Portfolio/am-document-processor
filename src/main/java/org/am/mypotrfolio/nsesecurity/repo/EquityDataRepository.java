package org.am.mypotrfolio.nsesecurity.repo;

import org.am.mypotrfolio.nsesecurity.entity.EquityDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquityDataRepository extends JpaRepository<EquityDataEntity, UUID> {
    Optional<EquityDataEntity> findByIsin(String isin);
    Optional<EquityDataEntity> findBySymbol(String symbol);
    
    @Query("SELECT e FROM EquityDataEntity e WHERE e.isin = :key OR e.symbol = :key")
    Optional<EquityDataEntity> findByKey(@Param("key") String key);
}