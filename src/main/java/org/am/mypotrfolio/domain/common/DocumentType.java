package org.am.mypotrfolio.domain.common;

public enum DocumentType {
    BROKER_PORTFOLIO("Broker_Portfolio"),
    MUTUAL_FUND("Mutual_Fund"),
    NPS_STATEMENT("NPS_Statement"),
    COMPANY_FINANCIAL_REPORT("Company_Financial_Report"),
    STOCK_PORTFOLIO("Stock_Portfolio"),
    NSE_INDICES("NSE_Indices");
    
    private String documentType;
        
    DocumentType(String documentType) {
        this.documentType = documentType;
    }

    public static DocumentType fromCode(String code) {
        for (DocumentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
    

    public String getCode() {
        return documentType;
    }

    public boolean isBrokerPortfolio() {
        return "Broker_Portfolio".equals(documentType);
    }

    public boolean isMutualFund() {
        return "Mutual_Fund".equals(documentType);
    }

    public boolean isNPStatement() {
        return "NPS_Statement".equals(documentType);
    }

    public boolean isCompanyFinancialReport() {
        return "Company_Financial_Report".equals(documentType);
    }

    public boolean isStockPortfolio() {
        return "Stock_Portfolio".equals(documentType);
    }

    public boolean isNseIndices() {
        return "NSE_Indices".equals(documentType);
    }
}
