package org.am.mypotrfolio.service;

import com.am.common.amcommondata.domain.enums.AssetType;
import com.am.common.amcommondata.domain.enums.BrokerType;
import com.am.common.amcommondata.model.PortfolioModel;
import com.am.common.amcommondata.model.asset.AssetModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.am.mypotrfolio.domain.DhanStockPortfolio;
import org.am.mypotrfolio.nsesecurity.domain.NseSecurity;
import org.am.mypotrfolio.nsesecurity.repo.NseSecurityRepository;
import org.am.mypotrfolio.processor.FileProcessorFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DhanService implements PortfolioService {
    private final FileProcessorFactory fileProcessorFactory;
    private final NseSecurityRepository nseSecurityRepository;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public PortfolioModel processPortfolioFile(MultipartFile file) {
        log.info("Starting to process portfolio file: {}", file.getOriginalFilename());
        try {
            // Process the file using appropriate processor
            log.debug("Getting file processor for file type");
            List<Map<String, String>> fileData = fileProcessorFactory.getProcessor(file)
                    .processFile(file, "Dhan");
            log.debug("Successfully processed file data, converting to DhanStockPortfolio objects");

            // Convert the data to DhanStockPortfolio objects
            String payload = objectMapper.writeValueAsString(fileData);
            List<DhanStockPortfolio> stocks = objectMapper.readValue(payload, 
                    new TypeReference<List<DhanStockPortfolio>>() {});
            log.info("Converted {} stocks from file", stocks.size());

            // Create portfolio assets with NSE security information
            Set<AssetModel> portfolioAssets = new HashSet<>();
            for (DhanStockPortfolio stock : stocks) {
                log.debug("Processing stock: {}", stock.getName());
                // Try to find NSE security by name or other identifiers
                Optional<NseSecurity> nseSecurity = nseSecurityRepository.findBestMatchBySearchParam(stock.getName());
            
                AssetModel.AssetModelBuilder assetBuilder = AssetModel.builder()
                        .assetType(AssetType.EQUITY)
                        .avgBuyingPrice(getDouble(stock.getAvgPrice()))
                        .quantity(Double.valueOf(stock.getQuantity()))
                        .investmentValue(getDouble(stock.getInvestmentValue()))
                        .name(stock.getName())
                        .buyingPlatform(BrokerType.DHAN.getCode());

                // Enhance asset with NSE security information if available
                if (nseSecurity.isPresent()) {
                    NseSecurity security = nseSecurity.get();
                    log.debug("Found NSE security information for: {}", stock.getName());
                    assetBuilder.isin(security.getIsin());
                    assetBuilder.symbol(security.getSecurityId());
                    assetBuilder.name(security.getSecurityName());
                }

                portfolioAssets.add(assetBuilder.build());
            }

            log.info("Successfully created portfolio with {} assets", portfolioAssets.size());
            // Create and return portfolio model
            return PortfolioModel.builder()
                    .assets(portfolioAssets)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error processing portfolio file: {}", e.getMessage(), e);
            throw e;
        }
    }

    private Double getDouble(String value) {
        return Double.parseDouble(value.replaceAll(",", ""));
    }
}
