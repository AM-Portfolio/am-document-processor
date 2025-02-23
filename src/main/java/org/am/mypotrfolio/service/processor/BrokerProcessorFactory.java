package org.am.mypotrfolio.service.processor;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BrokerProcessorFactory {
    private final Map<String, BrokerDocumentProcessor> processorMap;

    public BrokerProcessorFactory(List<BrokerDocumentProcessor> processors) {
        this.processorMap = processors.stream()
            .collect(Collectors.toMap(
                processor -> processor.getBrokerType().toUpperCase(),
                Function.identity()
            ));
    }

    public BrokerDocumentProcessor getProcessor(String brokerType) {
        BrokerDocumentProcessor processor = processorMap.get(brokerType.toUpperCase());
        if (processor == null) {
            throw new IllegalArgumentException("Unsupported broker type: " + brokerType);
        }
        return processor;
    }

    public List<String> getSupportedBrokers() {
        return List.copyOf(processorMap.keySet());
    }
}
