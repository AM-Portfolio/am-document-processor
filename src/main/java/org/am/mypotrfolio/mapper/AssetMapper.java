// package org.am.mypotrfolio.mapper;

// import org.am.mypotrfolio.domain.DhanStockPortfolio;
// import org.am.mypotrfolio.domain.MStockPortfolio;
// import org.am.mypotrfolio.domain.NseStock;
// import org.am.mypotrfolio.domain.ZerodhaStockPortfolio;

// import org.mapstruct.Mapper;
// import org.mapstruct.Mapping;
// import org.mapstruct.Named;
// import org.mapstruct.factory.Mappers;

// @Mapper(componentModel = "spring")
// public interface AssetMapper {
//     AssetMapper INSTANCE = Mappers.getMapper(AssetMapper.class);

//     @Mapping(source = "symbol", target = "symbol")
//     @Mapping(source = "quantityAvailable", target = "quantity")
//     @Mapping(source = "averagePrice", target = "avePrice")
//     @Mapping(expression = "java(stockInfo.getQuantityAvailable() * stockInfo.getAveragePrice())", target = "investedValue")
//     @Mapping(target = "currentValue", ignore = true)
//     @Mapping(target = "currentPrice", ignore = true)
//     @Mapping(target = "profitLoss", ignore = true)
//     @Mapping(target = "openPrice", ignore = true)
//     @Mapping(target = "id", ignore = true)
//     @Mapping(target = "brokerPlatform", constant = "ZERODHA")
//     @Mapping(target = "tradeType", ignore = true)
//     @Mapping(target = "userId", ignore = true)
//     NseStock toNseStockFromZerodha(ZerodhaStockPortfolio stockInfo);

//     @Mapping(source = "symbol", target = "symbol")
//     @Mapping(source = "isin", target = "isin")
//     @Mapping(source = "quantity", target = "quantity", qualifiedByName = "stringToDouble")
//     @Mapping(source = "avgPrice", target = "avePrice", qualifiedByName = "stringToDouble")
//     @Mapping(source = "investedValue", target = "investedValue", qualifiedByName = "stringToDouble")
//     @Mapping(target = "currentValue", ignore = true)
//     @Mapping(target = "currentPrice", ignore = true)
//     @Mapping(target = "profitLoss", ignore = true)
//     @Mapping(target = "openPrice", ignore = true)
//     @Mapping(target = "id", ignore = true)
//     @Mapping(target = "brokerPlatform", constant = "MSTOCK")
//     @Mapping(target = "tradeType", ignore = true)
//     @Mapping(target = "userId", ignore = true)
//     NseStock toNseStock(MStockPortfolio stockPortfolio);

//     @Mapping(source = "securityId", target = "symbol")
//     @Mapping(source = "quantity", target = "quantity")
//     @Mapping(source = "avgPrice", target = "avePrice")
//     @Mapping(expression = "java(stock.getQuantity() * stock.getAvgPrice())", target = "investedValue")
//     @Mapping(source = "isin", target = "isin")
//     @Mapping(target = "currentValue", ignore = true)
//     @Mapping(target = "currentPrice", ignore = true)
//     @Mapping(target = "profitLoss", ignore = true)
//     @Mapping(target = "openPrice", ignore = true)
//     @Mapping(target = "id", ignore = true)
//     @Mapping(target = "brokerPlatform", constant = "DHAN")
//     @Mapping(target = "tradeType", ignore = true)
//     @Mapping(target = "userId", ignore = true)
//     NseStock toNseStockFromDhan(DhanStockPortfolio stock);

//     @Mapping(source = "securityId", target = "symbol")
//     @Mapping(source = "quantity", target = "quantity")
//     @Mapping(source = "avgPrice", target = "avePrice")
//     @Mapping(expression = "java(stock.getQuantity() * stock.getAvgPrice())", target = "investedValue")
//     @Mapping(target = "currentValue", ignore = true)
//     @Mapping(target = "currentPrice", ignore = true)
//     @Mapping(target = "profitLoss", ignore = true)
//     @Mapping(target = "openPrice", ignore = true)
//     @Mapping(target = "id", ignore = true)
//     @Mapping(target = "brokerPlatform", constant = "DHAN")
//     @Mapping(target = "tradeType", ignore = true)
//     @Mapping(target = "userId", ignore = true)
//     NseStock mapNseStock(DhanStockPortfolio stock);

//     @Named("stringToDouble")
//     default double stringToDouble(String value) {
//         return value == null || value.isEmpty() ? 0.0 : Double.parseDouble(value);
//     }
// }

