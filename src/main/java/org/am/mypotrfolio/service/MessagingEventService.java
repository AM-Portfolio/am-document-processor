package org.am.mypotrfolio.service;

import java.util.Set;
import java.util.UUID;

import org.am.mypotrfolio.kafka.model.PortfolioUpdateEvent;
import org.am.mypotrfolio.kafka.producer.KafkaProducerService;
import org.am.mypotrfolio.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import com.am.common.amcommondata.model.asset.AssetModel;
import com.am.common.amcommondata.model.asset.mutualfund.MutualFundModel;
import com.am.common.amcommondata.model.enums.BrokerType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingEventService {
    private final KafkaProducerService kafkaProducerService;

    public void sendStockPortfolioMessage(Set<AssetModel> assetModels, UUID processId, BrokerType brokerType) {
       var portfolioUpdateEvent = buildPortfolioUpdateEvent(processId, brokerType);
       portfolioUpdateEvent.setAssets(assetModels);
       kafkaProducerService.sendMessage(portfolioUpdateEvent);
    }

    public void sendMutualFundPortfolioMessage(Set<MutualFundModel> mFundModels, UUID processId, BrokerType brokerType) {
        var portfolioUpdateEvent = buildPortfolioUpdateEvent(processId, brokerType);
        portfolioUpdateEvent.setMutualFunds(mFundModels);
        kafkaProducerService.sendMessage(portfolioUpdateEvent);
     }

    public void sendMessage(PortfolioUpdateEvent portfolioUpdateEvent, UUID processId, BrokerType brokerType) {
        log.info("[ProcessId: {}] Preparing to send portfolio update event and payload {}", processId, ObjectUtils.convertToJson(portfolioUpdateEvent));
        kafkaProducerService.sendMessage(portfolioUpdateEvent);
        log.info("[ProcessId: {}] Successfully sent portfolio update event", processId);
    }

    private PortfolioUpdateEvent buildPortfolioUpdateEvent(UUID processId, BrokerType brokerType) {
        return PortfolioUpdateEvent.builder()
                .id(processId)
                .userId("MKU257")
                .brokerType(brokerType)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}