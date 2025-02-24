package org.am.mypotrfolio.service;

import java.util.Set;
import java.util.UUID;

import org.am.mypotrfolio.kafka.model.PortfolioUpdateEvent;
import org.am.mypotrfolio.kafka.producer.KafkaProducerService;
import org.springframework.stereotype.Service;

import com.am.common.amcommondata.model.asset.AssetModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingEventService {
    private final KafkaProducerService kafkaProducerService;

    public void sendMessage(Set<AssetModel> assetModels, UUID processId) {
        log.info("[ProcessId: {}] Preparing to send portfolio update event", processId);
        PortfolioUpdateEvent portfolioUpdateEvent = PortfolioUpdateEvent.builder()
                .id(processId)
                .userId("MKU257")
                .assets(assetModels)
                .timestamp(System.currentTimeMillis())
                .build();
        
        log.debug("[ProcessId: {}] Sending portfolio update event to Kafka", processId);
        kafkaProducerService.sendMessage(portfolioUpdateEvent);
        log.info("[ProcessId: {}] Successfully sent portfolio update event", processId);
    }
}