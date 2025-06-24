package org.am.mypotrfolio.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.am.mypotrfolio.domain.common.DocumentRequest;
import org.am.mypotrfolio.mapper.TradeMapper;
import org.am.mypotrfolio.model.trade.Trade;
import org.am.mypotrfolio.model.trade.TradeModel;
import org.am.mypotrfolio.service.TradeService;
import org.springframework.stereotype.Service;
import org.am.mypotrfolio.processor.FileProcessorFactory;

import com.am.common.amcommondata.model.enums.BrokerType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the TradeService for processing trade files
 * Based on Zerodha's F&O trade book structure
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeServiceImpl implements TradeService {

    private final FileProcessorFactory fileProcessorFactory;
    private final ObjectMapper objectMapper;

    @Override
    public List<TradeModel> processTradeFile(DocumentRequest documentRequest) {
        
        log.info("[ProcessId: {}] Starting to process trade file: {}", documentRequest.getRequestId(), documentRequest.getFile().getOriginalFilename());
        try {
            // Process the file using appropriate processor
            log.debug("[ProcessId: {}] Getting file processor for file type", documentRequest.getRequestId());
            List<Map<String, String>> fileData = fileProcessorFactory.getProcessor(documentRequest.getFile())
                    .processFile(documentRequest.getFile(), documentRequest);
            log.debug("[ProcessId: {}] Successfully processed file data, converting to StockPortfolio objects", documentRequest.getRequestId());
            return processTradeFileAndGetTradeList(fileData, documentRequest.getBrokerType(), documentRequest.getRequestId());
        } catch (Exception e) {
            log.error("[ProcessId: {}] Error processing trade file: {}", documentRequest.getRequestId(), e.getMessage(), e);
            throw e;
        }
    }
    
    @SneakyThrows
    public List<TradeModel> processTradeFileAndGetTradeList(List<Map<String, String>> fileData, BrokerType brokerType, UUID processId) {
       TradeMapper tradeMapper = new TradeMapper();
        // Convert the data to StockPortfolio objects
       String payload = objectMapper.writeValueAsString(fileData);
       List<Trade> trades = objectMapper.readValue(payload, new TypeReference<List<Trade>>() {});
        // Convert to AssetModels
            List<TradeModel> tradeList = new ArrayList<>();
            for (Trade trade : trades) {
                log.debug("[ProcessId: {}] Processing trades: {}", processId);
                TradeModel tradeModel = tradeMapper.toTradeModel(trade);
                tradeList.add(tradeModel);
            }
       log.info("[ProcessId: {}] Successfully processed {} trade entries", processId, tradeList.size());
       return tradeList;
    }
}
