package org.am.mypotrfolio.model.trade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Enum representing different index types in the market
 */
public enum IndexType {
    NIFTY("NIFTY", "Nifty 50", new BigDecimal(50)),
    BANKNIFTY("BANKNIFTY", "Bank Nifty", new BigDecimal(25)),
    FINNIFTY("FINNIFTY", "Financial Services Nifty", new BigDecimal(40)),
    MIDCPNIFTY("MIDCPNIFTY", "Midcap Nifty", new BigDecimal(75)),
    UNKNOWN("UNKNOWN", "Unknown Index", new BigDecimal(0));

    private final String value;
    private final String description;
    private final BigDecimal lotSize;

    IndexType(String value, String description, BigDecimal lotSize) {
        this.value = value;
        this.description = description;
        this.lotSize = lotSize;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public BigDecimal getLotSize() {
        return lotSize;
    }

    @JsonCreator
    public static IndexType fromValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        
        return Arrays.stream(values())
                .filter(indexType -> indexType.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
    
    /**
     * Check if a symbol represents an index
     * 
     * @param symbol the symbol to check
     * @return true if the symbol is an index, false otherwise
     */
    public static boolean isIndex(String symbol) {
        if (symbol == null) {
            return false;
        }
        
        return Arrays.stream(values())
                .filter(indexType -> !indexType.equals(UNKNOWN))
                .anyMatch(indexType -> indexType.value.equalsIgnoreCase(symbol));
    }
    
    /**
     * Get the lot size for a given symbol
     * 
     * @param symbol the symbol to get the lot size for
     * @return the lot size for the symbol, or a default value if not found
     */
    public static BigDecimal getLotSizeForSymbol(String symbol) {
        if (symbol == null) {
            return BigDecimal.ONE;
        }
        
        return Arrays.stream(values())
                .filter(indexType -> indexType.value.equalsIgnoreCase(symbol))
                .findFirst()
                .map(IndexType::getLotSize)
                .orElse(new BigDecimal(1000)); // Default lot size for equity
    }
}
