package org.am.mypotrfolio.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DhanStockPortfolio {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Quantity")
    private int quantity;

    @JsonProperty("Avg Price")
    private String avgPrice;

    @JsonProperty("Last Traded")
    private String lastTraded;

    @JsonProperty("Investment")
    private String investmentValue;

    @JsonProperty("Current Value")
    private String currentValue;

    @JsonProperty("P&L")
    private String profitLoss;

    @JsonProperty("P&L %")
    private String profitLossPercentage;
}
