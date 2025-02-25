package org.am.mypotrfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.am.mypotrfolio.domain.MStockPortfolio;
import org.am.mypotrfolio.nsesecurity.domain.NseSecurity;
import org.am.mypotrfolio.nsesecurity.repo.NseSecurityRepository;
import org.am.mypotrfolio.processor.FileProcessorFactory;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.asset.AssetModel;
import com.am.common.amcommondata.model.enums.AssetType;
import com.am.common.amcommondata.model.enums.BrokerType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;

@Service("MStock")
@Slf4j
@ComponentScan
@RequiredArgsConstructor
public class MStockService implements PortfolioService {

    private final FileProcessorFactory fileProcessorFactory;
    private final NseSecurityRepository nseSecurityRepository;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("rawtypes")
    @SneakyThrows
    @Override
    public Set<AssetModel> processPortfolioFile(MultipartFile file, UUID processId) {
        log.info("[ProcessId: {}] Starting to process portfolio file: {}", processId, file.getOriginalFilename());
        try {
            // Process the file using appropriate processor
            log.debug("[ProcessId: {}] Getting file processor for file type", processId);
            List<Map<String, String>> fileData = fileProcessorFactory.getProcessor(file)
                    .processFile(file, "MStock");
            log.debug("[ProcessId: {}] Successfully processed file data, converting to MStockPortfolio objects", processId);

            // Convert the data to MStockPortfolio objects
            String payload = objectMapper.writeValueAsString(fileData);
            List<MStockPortfolio> stocks = objectMapper.readValue(payload, 
                    new TypeReference<List<MStockPortfolio>>() {});
            log.info("[ProcessId: {}] Converted {} stocks from file", processId, stocks.size());

            // Create portfolio assets with NSE security information
            Set<AssetModel> portfolioAssets = new HashSet<>();
            for (MStockPortfolio stock : stocks) {
                log.debug("[ProcessId: {}] Processing stock: {}", processId, stock.getSymbol());
                // Try to find NSE security by name or other identifiers
                Optional<NseSecurity> nseSecurity = nseSecurityRepository.findBestMatchBySearchParam(stock.getSymbol());
            
                
                AssetModel.AssetModelBuilder assetBuilder = AssetModel.builder()
                        .assetType(AssetType.EQUITY)
                        .avgBuyingPrice(stock.getAvgPrice())
                        .quantity(Double.valueOf(stock.getQuantity()))
                        .investmentValue(stock.getInvestedValue())
                        .name(stock.getSymbol())
                        .buyingPlatform(BrokerType.MSTOCK.getCode());

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
}
