package org.am.mypotrfolio.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.am.mypotrfolio.domain.common.StockPortfolio;
import org.am.mypotrfolio.nsesecurity.domain.NseSecurity;
import org.am.mypotrfolio.nsesecurity.repo.NseSecurityRepository;
import org.am.mypotrfolio.processor.FileProcessorFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.asset.AssetModel;
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

    @SuppressWarnings("rawtypes")
    @SneakyThrows
    @Override
    public Set<AssetModel> processPortfolioFile(MultipartFile file, UUID processId, BrokerType brokerType) {
        log.info("[ProcessId: {}] Starting to process portfolio file: {}", processId, file.getOriginalFilename());
        try {
            // Process the file using appropriate processor
            log.debug("[ProcessId: {}] Getting file processor for file type", processId);
            List<Map<String, String>> fileData = fileProcessorFactory.getProcessor(file)
                    .processFile(file, brokerType);
            log.debug("[ProcessId: {}] Successfully processed file data, converting to StockPortfolio objects", processId);

            // Convert the data to StockPortfolio objects
            String payload = objectMapper.writeValueAsString(fileData);
            List<StockPortfolio> portfolios = objectMapper.readValue(payload,
                    new TypeReference<List<StockPortfolio>>() {});

            // Convert to AssetModels
            Set<AssetModel> portfolioAssets = new HashSet<>();
            for (StockPortfolio stock : portfolios) {
                log.debug("[ProcessId: {}] Processing stock: {}", processId, stock.getSymbol());
                // Try to find NSE security by name or other identifiers
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
                        log.debug("[ProcessId: {}] Found NSE security information for: {}", processId,
                                stock.getSymbol());
                        assetBuilder.isin(security.getIsin());
                        assetBuilder.symbol(security.getSecurityId());
                        assetBuilder.name(security.getSecurityName());
                    }
                }

                portfolioAssets.add(assetBuilder.build());
            }

            log.info("[ProcessId: {}] Successfully processed {} portfolio entries", processId, portfolioAssets.size());
            return portfolioAssets;
        } catch (Exception e) {
            log.error("[ProcessId: {}] Error processing portfolio file: {}", processId, e.getMessage(), e);
            throw e;
        }
    }
}
