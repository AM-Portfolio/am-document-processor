package org.am.mypotrfolio.service.processor;

import org.springframework.stereotype.Component;

import com.am.common.amcommondata.model.enums.BrokerType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BrokerProcessorFactory {
    private final Map<BrokerType, BrokerDocumentProcessor> processorMap;

    public BrokerProcessorFactory(List<BrokerDocumentProcessor> processors) {
        this.processorMap = processors.stream()
            .collect(Collectors.toMap(
                processor -> processor.getBrokerType(),
                Function.identity()
            ));
    }

    public BrokerDocumentProcessor getProcessor(BrokerType brokerType) {
        BrokerDocumentProcessor processor = processorMap.get(brokerType);
        if (processor == null) {
            throw new IllegalArgumentException("Unsupported broker type: " + brokerType);
        }
        return processor;
    }

    public List<BrokerType> getSupportedBrokers() {
        return List.copyOf(processorMap.keySet());
    }
}
