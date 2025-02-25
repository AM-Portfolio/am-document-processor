package org.am.mypotrfolio.service.processor;

import java.util.Set;
import java.util.UUID;

import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.service.MStockService;
import org.am.mypotrfolio.service.MessagingEventService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.asset.AssetModel;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MStockDocumentProcessor implements BrokerDocumentProcessor {
    private final MStockService mStockService;
    private final MessagingEventService messagingEventService;

    @Override
    public DocumentProcessResponse processDocument(MultipartFile file, UUID processId) {
        // Call ZerodhaService specific processing logic
        Set<AssetModel> assets = mStockService.processPortfolioFile(file, processId);
        messagingEventService.sendMessage(assets, processId);
        // Create response
        DocumentProcessResponse response = new DocumentProcessResponse();
        response.setDocumentType("MSTOCK_PORTFOLIO");
        response.setFileName(file.getOriginalFilename());
        response.setProcessId(processId);
        return response;
    }

    @Override
    public String getBrokerType() {
        return "MSTOCK";
    }
}
