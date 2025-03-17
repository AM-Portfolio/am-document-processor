package org.am.mypotrfolio.nsesecurity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.am.common.amcommondata.model.MarketCapType;

@Entity
@Table(name = "equity_data")
@Getter
@Setter
public class EquityDataEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false)
    private String symbol;

    @Column(unique = true)
    private String isin;
    
    @Column(nullable = false)
    private String name;
    
    private String series;
    
    private Double faceValue;

    private Double marketCap;
    
    private String industry;

    private String sector;
    
    @Column(name = "instrument_type")
    private String instrumentType;
    
    @Enumerated(EnumType.STRING)
    private MarketCapType marketCapType;
    
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
    
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
    
    private static final Logger log = LoggerFactory.getLogger(EquityDataEntity.class);

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
        updateMarketCapType();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
        updateMarketCapType();
    }
    
    public void updateMarketCapType() {
        if (marketCap != null && marketCap > 0) {
            // Calculate market cap type based on market cap value
            if (marketCap >= 1000000000000D) { // > 1 lakh crore
                marketCapType = MarketCapType.LARGE_CAP;
            } else if (marketCap >= 250000000000D) { // > 25k crore
                marketCapType = MarketCapType.MID_CAP;
            } else if (marketCap >= 50000000000D) { // > 5k crore
                marketCapType = MarketCapType.SMALL_CAP;
            } else {
                marketCapType = MarketCapType.MICRO_CAP;
            }
            log.info("Setting market cap type for {}: {} (Market Cap: {})", symbol, marketCapType, marketCap);
        } else {
            marketCapType = MarketCapType.MICRO_CAP;
            log.info("No market cap available for {}, defaulting to MICRO_CAP", symbol);
        }
    }
} 