package org.am.mypotrfolio.nsesecurity.repo;

import org.am.mypotrfolio.nsesecurity.domain.NseSecurity;
import org.am.mypotrfolio.nsesecurity.entity.NseSecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NseSecurityRepository extends JpaRepository<NseSecurityEntity, UUID> {
    
    @Query("SELECT new org.am.mypotrfolio.nsesecurity.domain.NseSecurity(" +
           "n.securityId, n.securityName, n.status, n.series, n.isin, n.faceValue, " +
           "n.industry, n.instrumentType, n.sectorName, n.industryNewName, n.iGroupName, " +
           "n.iSubGroupName, n.sectorIndices, n.thematicIndices, n.marketIndices) " +
           "FROM NseSecurityEntity n WHERE n.isin = :isin AND n.status = 'Active'")
    Optional<NseSecurity> findByIsin(@Param("isin") String isin);

    @Query("SELECT new org.am.mypotrfolio.nsesecurity.domain.NseSecurity(" +
           "n.securityId, n.securityName, n.status, n.series, n.isin, n.faceValue, " +
           "n.industry, n.instrumentType, n.sectorName, n.industryNewName, n.iGroupName, " +
           "n.iSubGroupName, n.sectorIndices, n.thematicIndices, n.marketIndices) " +
           "FROM NseSecurityEntity n WHERE LOWER(n.securityName) = LOWER(:securityName) AND n.status = 'Active'")
    Optional<NseSecurity> findBySecurityName(@Param("securityName") String securityName);

    @Query("SELECT new org.am.mypotrfolio.nsesecurity.domain.NseSecurity(" +
           "n.securityId, n.securityName, n.status, n.series, n.isin, n.faceValue, " +
           "n.industry, n.instrumentType, n.sectorName, n.industryNewName, n.iGroupName, " +
           "n.iSubGroupName, n.sectorIndices, n.thematicIndices, n.marketIndices) " +
           "FROM NseSecurityEntity n WHERE LOWER(n.securityName) LIKE LOWER(CONCAT('%', :partialName, '%')) AND n.status = 'Active' ORDER BY LENGTH(n.securityName)")
    List<NseSecurity> findBySecurityNameFuzzy(@Param("partialName") String partialName);

    @Query("SELECT new org.am.mypotrfolio.nsesecurity.domain.NseSecurity(" +
           "n.securityId, n.securityName, n.status, n.series, n.isin, n.faceValue, " +
           "n.industry, n.instrumentType, n.sectorName, n.industryNewName, n.iGroupName, " +
           "n.iSubGroupName, n.sectorIndices, n.thematicIndices, n.marketIndices) " +
           "FROM NseSecurityEntity n WHERE n.securityId = :securityId AND n.status = 'Active'")
    Optional<NseSecurity> findBySecurityId(@Param("securityId") String securityId);

    @Query("SELECT new org.am.mypotrfolio.nsesecurity.domain.NseSecurity(" +
           "n.securityId, n.securityName, n.status, n.series, n.isin, n.faceValue, " +
           "n.industry, n.instrumentType, n.sectorName, n.industryNewName, n.iGroupName, " +
           "n.iSubGroupName, n.sectorIndices, n.thematicIndices, n.marketIndices) " +
           "FROM NseSecurityEntity n WHERE n.marketIndices LIKE CONCAT('%', :indexType, '%')")
    List<NseSecurity> findByMarketIndices(@Param("indexType") String marketIndices);

    @Query("SELECT new org.am.mypotrfolio.nsesecurity.domain.NseSecurity(" +
           "n.securityId, n.securityName, n.status, n.series, n.isin, n.faceValue, " +
           "n.industry, n.instrumentType, n.sectorName, n.industryNewName, n.iGroupName, " +
           "n.iSubGroupName, n.sectorIndices, n.thematicIndices, n.marketIndices) " +
           "FROM NseSecurityEntity n WHERE n.status = 'Active'")
    List<NseSecurity> findAllActiveSecurities();

    @Query("""
            SELECT new org.am.mypotrfolio.nsesecurity.domain.NseSecurity(
                n.securityId, n.securityName, n.status, n.series, n.isin, n.faceValue, 
                n.industry, n.instrumentType, n.sectorName, n.industryNewName, n.iGroupName, 
                n.iSubGroupName, n.sectorIndices, n.thematicIndices, n.marketIndices)
            FROM NseSecurityEntity n 
            WHERE (
                :searchParam = n.isin OR 
                :searchParam = n.securityId OR 
                LOWER(n.securityName) LIKE LOWER(CONCAT('%', :searchParam, '%'))
            )
            AND n.status = 'Active'
            ORDER BY 
                CASE 
                    WHEN n.isin = :searchParam THEN 1
                    WHEN n.securityId = :searchParam THEN 2
                    WHEN LOWER(n.securityName) = LOWER(:searchParam) THEN 3
                    ELSE 4
                END,
                LENGTH(n.securityName)
            """)
    List<NseSecurity> findSecurityBySearchParam(@Param("searchParam") String searchParam);

    default Optional<NseSecurity> findBestMatchBySearchParam(String searchParam) {
        if (searchParam == null || searchParam.trim().isEmpty()) {
            return Optional.empty();
        }
        List<NseSecurity> matches = findSecurityBySearchParam(searchParam.trim());
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }
}