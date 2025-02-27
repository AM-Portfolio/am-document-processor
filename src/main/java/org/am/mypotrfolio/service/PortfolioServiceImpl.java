package org.am.mypotrfolio.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.am.mypotrfolio.domain.common.MutualFundAsset;
import org.am.mypotrfolio.domain.common.PortfolioRequest;
import org.am.mypotrfolio.domain.common.StockAsset;
import org.am.mypotrfolio.nsesecurity.domain.NseSecurity;
import org.am.mypotrfolio.nsesecurity.repo.NseSecurityRepository;
import org.am.mypotrfolio.processor.FileProcessorFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.asset.AssetModel;
import com.am.common.amcommondata.model.asset.mutualfund.MutualFundModel;
import com.am.common.amcommondata.model.enums.AssetType;
import com.am.common.amcommondata.model.enums.BrokerType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {
    private final FileProcessorFactory fileProcessorFactory;
    private final NseSecurityRepository nseSecurityRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Set<AssetModel> processEquityFile(PortfolioRequest portfolioRequest) {
        
        log.info("[ProcessId: {}] Starting to process stokcs portfolio file: {}", portfolioRequest.getRequestId(), portfolioRequest.getFile().getOriginalFilename());
        try {
            // Process the file using appropriate processor
            log.debug("[ProcessId: {}] Getting file processor for file type", portfolioRequest.getRequestId());
            List<Map<String, String>> fileData = fileProcessorFactory.getProcessor(portfolioRequest.getFile())
                    .processFile(portfolioRequest.getFile(), portfolioRequest.getBrokerType());
            log.debug("[ProcessId: {}] Successfully processed file data, converting to StockPortfolio objects", portfolioRequest.getRequestId());
            return processPortfolioFileAndGetAssets(fileData, portfolioRequest.getBrokerType(), portfolioRequest.getRequestId());
        } catch (Exception e) {
            log.error("[ProcessId: {}] Error processing portfolio file: {}", portfolioRequest.getRequestId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Set<MutualFundModel> processMutualFundFile(PortfolioRequest portfolioRequest) {
        log.info("[ProcessId: {}] Starting to process mutual funds portfolio file: {}", portfolioRequest.getRequestId(), portfolioRequest.getFile().getOriginalFilename());
        try {
            // Process the file using appropriate processor
            log.debug("[ProcessId: {}] Getting file processor for file type", portfolioRequest.getRequestId());
            List<Map<String, String>> fileData = fileProcessorFactory.getProcessor(portfolioRequest.getFile())
                    .processFile(portfolioRequest.getFile(), portfolioRequest.getBrokerType());
            log.debug("[ProcessId: {}] Successfully processed file data, converting to StockPortfolio objects", portfolioRequest.getRequestId());
            return processMutualFundsPortfolioFileAndGetAssets(fileData, portfolioRequest.getBrokerType(), portfolioRequest.getRequestId());
        } catch (Exception e) {
            log.error("[ProcessId: {}] Error processing portfolio file: {}", portfolioRequest.getRequestId(), e.getMessage(), e);
            throw e;
        }
    }
    
    @SneakyThrows
    public Set<MutualFundModel> processMutualFundsPortfolioFileAndGetAssets(List<Map<String, String>> fileData, BrokerType brokerType, UUID processId) {
       // Convert the data to StockPortfolio objects
       String payload = objectMapper.writeValueAsString(fileData);
       List<MutualFundAsset> portfolios = objectMapper.readValue(payload, new TypeReference<List<MutualFundAsset>>() {});
        // Convert to AssetModels
            Set<MutualFundModel> portfolioAssets = new HashSet<>();
            for (MutualFundAsset mutualFund : portfolios) {
                log.debug("[ProcessId: {}] Processing mutual funds: {}", processId, mutualFund.getSchemeName());
                // Try to find NSE security by name or other identifiers
                var assetModel = getMutualFundModel(mutualFund, brokerType);
                portfolioAssets.add(assetModel);
            }
       log.info("[ProcessId: {}] Successfully processed {} portfolio entries", processId, portfolioAssets.size());
       return portfolioAssets;
    }

    @SuppressWarnings("rawtypes")
    private MutualFundModel getMutualFundModel(MutualFundAsset mutualFund, BrokerType brokerType) {
        var quantity = getDouble(mutualFund.getUnits());
        var investedValue = mutualFund.getInvestedValue() != null ? getDouble(mutualFund.getInvestedValue()) : 0.0;
        var currentValue = mutualFund.getCurrentValue() != null ? getDouble(mutualFund.getCurrentValue()) : 0.0;
        MutualFundModel.MutualFundModelBuilder assetBuilder = MutualFundModel.builder()
                .assetType(AssetType.MUTUAL_FUND)
                .name(mutualFund.getSchemeName())
                .fundHouse(mutualFund.getAmc())
                .category(mutualFund.getCategory())
                .subCategory(mutualFund.getSubCategory())
                .quantity(quantity)
                .investmentValue(investedValue)
                .currentValue(currentValue);
        return assetBuilder.build();
    }

    @SneakyThrows
    public Set<AssetModel> processPortfolioFileAndGetAssets(List<Map<String, String>> fileData, BrokerType brokerType, UUID processId) {
       // Convert the data to StockPortfolio objects
       String payload = objectMapper.writeValueAsString(fileData);
       List<StockAsset> portfolios = objectMapper.readValue(payload, new TypeReference<List<StockAsset>>() {});
        // Convert to AssetModels
            Set<AssetModel> portfolioAssets = new HashSet<>();
            for (StockAsset stock : portfolios) {
                log.debug("[ProcessId: {}] Processing stock: {}", processId, stock.getSymbol());
                // Try to find NSE security by name or other identifiers
                var assetModel = getAssetModel(stock, brokerType);
                portfolioAssets.add(assetModel);
            }
       log.info("[ProcessId: {}] Successfully processed {} portfolio entries", processId, portfolioAssets.size());
       return portfolioAssets;
    }

    @SuppressWarnings("rawtypes")
    private AssetModel getAssetModel(StockAsset stock, BrokerType brokerType) {
        var quantity = getDouble(stock.getQuantity());
        var avgBuyingPrice = stock.getAvgPrice() != null ? getDouble(stock.getAvgPrice()) : 0.0;
        var investedValue = stock.getInvestmentValue() != null ? getDouble(stock.getInvestmentValue())
                : quantity * avgBuyingPrice;
        AssetModel.AssetModelBuilder assetBuilder = AssetModel.builder()
                .assetType(AssetType.EQUITY)
                .isin(stock.getIsin())
                .symbol(stock.getSymbol())
                .avgBuyingPrice(avgBuyingPrice)
                .quantity(quantity)
                .investmentValue(investedValue)
                .name(stock.getName());

        if (stock.getIsin() == null || stock.getIsin().isEmpty()) {
            Optional<NseSecurity> nseSecurity = nseSecurityRepository
                    .findBestMatchBySearchParam(brokerType.isDhan() ? stock.getName() : stock.getSymbol());
            // Enhance asset with NSE security information if available
            if (nseSecurity.isPresent()) {
                NseSecurity security = nseSecurity.get();
                assetBuilder.isin(security.getIsin());
                assetBuilder.symbol(security.getSecurityId());
                assetBuilder.name(security.getSecurityName());
            }
        }
        return assetBuilder.build();
    }
}
