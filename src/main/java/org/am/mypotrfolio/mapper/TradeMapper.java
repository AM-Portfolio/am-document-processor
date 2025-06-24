package org.am.mypotrfolio.mapper;

import org.am.mypotrfolio.model.trade.Trade;
import org.am.mypotrfolio.model.trade.TradeModel;
import org.springframework.stereotype.Component;

/**
 * Mapper utility class for converting between Trade and TradeModel objects.
 */
@Component
public class TradeMapper {

    /**
     * Converts a Trade object to a TradeModel object.
     *
     * @param trade the Trade object to convert
     * @return the converted TradeModel object
     */
    public TradeModel toTradeModel(Trade trade) {
        if (trade == null) {
            return null;
        }

        return TradeModel.builder()
                .basicInfo(buildBasicInfo(trade))
                .instrumentInfo(buildInstrumentInfo(trade))
                .executionInfo(buildExecutionInfo(trade))
                .build();
    }

    private TradeModel.BasicInfo buildBasicInfo(Trade trade) {
        return TradeModel.BasicInfo.builder()
                .tradeId(trade.getTradeId())
                .orderId(trade.getOrderId())
                .tradeDate(trade.getTradeDate())
                .orderExecutionTime(trade.getOrderExecutionTime())
                // Default values for fields not in Trade class
                .brokerType(null) // Set appropriate broker type if available
                .tradeType(null)   // Set appropriate trade type if available
                .build();
    }


    private TradeModel.InstrumentInfo buildInstrumentInfo(Trade trade) {
        return TradeModel.InstrumentInfo.builder()
                .symbol(trade.getSymbol())
                .isin(trade.getIsin())
                .exchange(trade.getExchange())
                .segment(trade.getSegment())
                .series(trade.getSeries())
                .build();
    }

    private TradeModel.ExecutionInfo buildExecutionInfo(Trade trade) {
        return TradeModel.ExecutionInfo.builder()
                .auction(trade.getAuction())
                .quantity(trade.getQuantity().intValue())
                .price(trade.getPrice())
                .build();
    }
}
