package org.am.mypotrfolio.service.processor;

import java.util.Set;
import java.util.UUID;

import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.service.MessagingEventService;
import org.am.mypotrfolio.service.PortfolioService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.asset.AssetModel;
import com.am.common.amcommondata.model.enums.BrokerType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DhanDocumentProcessor implements BrokerDocumentProcessor {
    private final PortfolioService portfolioService;
    private final MessagingEventService messagingEventService;

    @Override
    public DocumentProcessResponse processDocument(MultipartFile file, UUID processId, BrokerType brokerType) {
        // Call DhanService specific processing logic
        Set<AssetModel> assets = portfolioService.processPortfolioFile(file, processId, brokerType);
        messagingEventService.sendMessage(assets, processId, brokerType);
        // Create response
        DocumentProcessResponse response = new DocumentProcessResponse();
        response.setDocumentType(brokerType.name() + "_PORTFOLIO");
        response.setFileName(file.getOriginalFilename());
        response.setProcessId(processId);
        return response;
    }

    @Override
    public BrokerType getBrokerType() {
        return BrokerType.DHAN;
    }
}
