package org.am.mypotrfolio.service;

// Lombok annotations
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

// Project imports
import org.am.mypotrfolio.domain.ZerodhaStockPortfolio;
import org.am.mypotrfolio.nsesecurity.domain.NseSecurity;
import org.am.mypotrfolio.nsesecurity.repo.NseSecurityRepository;
import org.am.mypotrfolio.processor.FileProcessorFactory;
// Spring imports
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.asset.AssetModel;
import com.am.common.amcommondata.model.enums.AssetType;
import com.am.common.amcommondata.model.enums.BrokerType;
// Jackson imports
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
// Java core imports
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;

@Slf4j
@Service("Zerodha")
@RequiredArgsConstructor
public class ZerodhaService implements PortfolioService {
    private final FileProcessorFactory fileProcessorFactory;
    private final NseSecurityRepository nseSecurityRepository;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    @SuppressWarnings("rawtypes")
    public Set<AssetModel> processPortfolioFile(MultipartFile file, UUID processId) {
        log.info("[ProcessId: {}] Starting to process portfolio file: {}", processId, file.getOriginalFilename());
        try {
            // Process the file using appropriate processor
            log.debug("[ProcessId: {}] Getting file processor for file type", processId);
            List<Map<String, String>> fileData = fileProcessorFactory.getProcessor(file)
                    .processFile(file, "Zerodha");
            log.debug("[ProcessId: {}] Successfully processed file data, converting to ZerodhaStockPortfolio objects", processId);

            // Convert the data to ZerodhaStockPortfolio objects
            String payload = objectMapper.writeValueAsString(fileData);
            List<ZerodhaStockPortfolio> stocks = objectMapper.readValue(payload, 
                    new TypeReference<List<ZerodhaStockPortfolio>>() {});
            log.info("[ProcessId: {}] Converted {} stocks from file", processId, stocks.size());

            // Create portfolio assets with NSE security information
            Set<AssetModel> portfolioAssets = new HashSet<>();
            for (ZerodhaStockPortfolio stock : stocks) {
                log.debug("[ProcessId: {}] Processing stock: {}", processId, stock.getSymbol());
                // Try to find NSE security by name or other identifiers
                Optional<NseSecurity> nseSecurity = nseSecurityRepository.findBestMatchBySearchParam(stock.getSymbol());
            
                
                AssetModel.AssetModelBuilder assetBuilder = AssetModel.builder()
                        .assetType(AssetType.EQUITY)
                        .avgBuyingPrice(getDouble(stock.getAveragePrice()))
                        .quantity(stock.getQuantity())
                        .investmentValue(getDouble(stock.getAveragePrice()) * stock.getQuantity())
                        .name(stock.getSymbol())
                        .buyingPlatform(BrokerType.ZERODHA.getCode());

                // Enhance asset with NSE security information if available
                if (nseSecurity.isPresent()) {
                    NseSecurity security = nseSecurity.get();
                    log.debug("[ProcessId: {}] Found NSE security information for: {}", processId, stock.getSymbol());
                    assetBuilder.isin(security.getIsin());
                    assetBuilder.symbol(security.getSecurityId());
                    assetBuilder.name(security.getSecurityName());
                }

                portfolioAssets.add(assetBuilder.build());
            }

            log.info("[ProcessId: {}] Successfully created portfolio with {} assets", processId, portfolioAssets.size());
            // Create and return portfolio model
            return portfolioAssets;
        } catch (Exception e) {
            log.error("[ProcessId: {}] Error processing portfolio file: {}", processId, e.getMessage(), e);
            throw e;
        }
    }

    private Double getDouble(String value) {
        return Double.parseDouble(value.replaceAll(",", ""));
    }
}
